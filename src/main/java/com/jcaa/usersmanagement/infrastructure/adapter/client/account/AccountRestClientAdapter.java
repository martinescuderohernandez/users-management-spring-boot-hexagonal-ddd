package com.jcaa.usersmanagement.infrastructure.adapter.client.account;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcaa.usersmanagement.application.port.out.AccountClientPort;
import com.jcaa.usersmanagement.application.service.dto.command.CreateAccountCommand;
import com.jcaa.usersmanagement.application.service.dto.response.AccountResponse;
import com.jcaa.usersmanagement.domain.exception.AccountConflictException;
import com.jcaa.usersmanagement.domain.exception.AccountIntegrationException;
import com.jcaa.usersmanagement.domain.exception.AccountServiceException;
import com.jcaa.usersmanagement.domain.exception.AccountTimeoutException;
import com.jcaa.usersmanagement.domain.exception.AccountValidationException;
import com.jcaa.usersmanagement.infrastructure.adapter.client.account.dto.AccountCreationRestRequest;
import com.jcaa.usersmanagement.infrastructure.adapter.client.account.dto.AccountRestResponse;
import com.jcaa.usersmanagement.infrastructure.adapter.client.account.mapper.AccountClientMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Adaptador de salida (driven adapter) que implementa {@link AccountClientPort}
 * ejecutando la llamada HTTP POST síncrona hacia {@code /accounts} en "Account Microservice".
 *
 * <p>Maneja adecuadamente:
 * <ul>
 *   <li>Serialización y deserialización JSON con Jackson</li>
 *   <li>Respuestas exitosas HTTP 201 Created</li>
 *   <li>Errores HTTP 409 Conflict (número de cuenta o ID duplicado)</li>
 *   <li>Errores HTTP 422 Unprocessable Entity (validaciones o campos obligatorios faltantes)</li>
 *   <li>Errores HTTP 500 / 5xx Internal Server Error (fallo en BD o servidor externo)</li>
 *   <li>Timeouts de conexión o lectura (mapeados a {@link AccountTimeoutException})</li>
 *   <li>Fallas de red y conectividad (mapeados a {@link AccountServiceException})</li>
 * </ul>
 */
@Slf4j
@Component
public class AccountRestClientAdapter implements AccountClientPort {

  private static final String ACCOUNTS_PATH = "/accounts";

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  public AccountRestClientAdapter(
      @Qualifier("accountRestClient") final RestClient restClient,
      final ObjectMapper objectMapper) {
    this.restClient = restClient;
    this.objectMapper = objectMapper;
  }

  @Override
  public AccountResponse createAccount(final CreateAccountCommand command) {
    if (command == null) {
      throw AccountValidationException.of("El comando para crear la cuenta no puede ser nulo.");
    }

    log.info("[AccountClient] Enviando petición síncrona POST {} para user_id={}", ACCOUNTS_PATH, command.userId());
    final AccountCreationRestRequest requestDto = AccountClientMapper.toRestRequest(command);

    try {
      final AccountRestResponse responseDto = restClient.post()
          .uri(ACCOUNTS_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .body(requestDto)
          .retrieve()
          .onStatus(status -> status.value() == 409, (request, response) -> {
            final String detail = extractDetail(response.getBody());
            log.warn("[AccountClient] Conflicto HTTP 409 recibido: {}", detail);
            throw AccountConflictException.of(detail);
          })
          .onStatus(status -> status.value() == 422, (request, response) -> {
            final String detail = extractDetail(response.getBody());
            log.warn("[AccountClient] Entidad no procesable HTTP 422 recibida: {}", detail);
            throw AccountValidationException.of(detail);
          })
          .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
            final String detail = extractDetail(response.getBody());
            log.error("[AccountClient] Error del servidor remoto HTTP {} recibido: {}",
                response.getStatusCode().value(), detail);
            throw AccountServiceException.becauseRemoteServerError(response.getStatusCode().value(), detail);
          })
          .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
            final String detail = extractDetail(response.getBody());
            log.error("[AccountClient] Error HTTP 4xx ({}) no esperado: {}",
                response.getStatusCode().value(), detail);
            throw AccountServiceException.becauseClientError(response.getStatusCode().value(), detail);
          })
          .body(AccountRestResponse.class);

      if (responseDto == null) {
        throw AccountServiceException.becauseNullResponse();
      }

      log.info("[AccountClient] Cuenta creada exitosamente. id={}, account_number={}",
          responseDto.id(), responseDto.accountNumber());
      return AccountClientMapper.toResponse(responseDto);

    } catch (final AccountIntegrationException ex) {
      throw ex;
    } catch (final ResourceAccessException ex) {
      log.error("[AccountClient] Error de conectividad/timeout con Account Microservice: {}", ex.getMessage(), ex);
      if (isTimeout(ex)) {
        throw AccountTimeoutException.of("Tiempo de espera agotado al comunicarse con Account Microservice.", ex);
      }
      throw AccountServiceException.becauseConnectionFailed("Fallo de conexión de red con Account Microservice.", ex);
    } catch (final RestClientException ex) {
      log.error("[AccountClient] Error inesperado en RestClient: {}", ex.getMessage(), ex);
      throw AccountServiceException.becauseUnexpectedError("Error inesperado en la comunicación con Account Microservice.", ex);
    }
  }

  private boolean isTimeout(final ResourceAccessException ex) {
    final Throwable cause = ex.getCause();
    if (cause instanceof SocketTimeoutException) {
      return true;
    }
    final String message = ex.getMessage();
    return message != null && (message.toLowerCase().contains("timeout") || message.toLowerCase().contains("timed out"));
  }

  private String extractDetail(final InputStream responseStream) {
    if (responseStream == null) {
      return "Sin detalle disponible";
    }
    try {
      final byte[] bytes = responseStream.readAllBytes();
      if (bytes.length == 0) {
        return "Respuesta vacía del microservicio";
      }
      final String rawBody = new String(bytes, StandardCharsets.UTF_8);
      try {
        final JsonNode root = objectMapper.readTree(rawBody);
        if (root.has("detail")) {
          final JsonNode detailNode = root.get("detail");
          if (detailNode.isTextual()) {
            return detailNode.asText();
          }
          return detailNode.toString();
        }
      } catch (final IOException ignored) {
        // En caso de que no sea JSON válido, se retorna el texto plano recibido
      }
      return rawBody;
    } catch (final IOException e) {
      return "No fue posible leer el detalle del error: " + e.getMessage();
    }
  }
}
