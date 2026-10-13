package com.jcaa.usersmanagement.infrastructure.adapter.client.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jcaa.usersmanagement.application.service.dto.command.CreateAccountCommand;
import com.jcaa.usersmanagement.application.service.dto.response.AccountResponse;
import com.jcaa.usersmanagement.domain.enums.AccountType;
import com.jcaa.usersmanagement.domain.exception.AccountConflictException;
import com.jcaa.usersmanagement.domain.exception.AccountServiceException;
import com.jcaa.usersmanagement.domain.exception.AccountTimeoutException;
import com.jcaa.usersmanagement.domain.exception.AccountValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@DisplayName("AccountRestClientAdapter")
class AccountRestClientAdapterTest {

  private static final String BASE_URL = "http://localhost:8000";

  private MockRestServiceServer mockServer;
  private AccountRestClientAdapter adapter;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    final RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
    mockServer = MockRestServiceServer.bindTo(builder).build();
    final RestClient restClient = builder.build();

    adapter = new AccountRestClientAdapter(restClient, objectMapper);
  }

  @Test
  @DisplayName("Debe crear la cuenta exitosamente cuando el microservicio retorna HTTP 201 Created")
  void shouldSuccessfullyCreateAccountWhenRemoteServiceReturns201() {
    final CreateAccountCommand command = CreateAccountCommand.builder()
        .userId("user-uuid-123")
        .accountType(AccountType.SAVINGS)
        .balance(new BigDecimal("150000.50"))
        .currency("COP")
        .userEmail("usuario@ejemplo.com")
        .build();

    final String mockJsonResponse = """
        {
          "id": "e3b0c442-98fc-1c14-9afbf4c8996fb924",
          "account_number": "1002345678",
          "user_id": "user-uuid-123",
          "account_type": "SAVINGS",
          "balance": 150000.50,
          "currency": "COP",
          "status": "ACTIVE",
          "created_at": "2026-10-12T17:30:00",
          "updated_at": "2026-10-12T17:30:00"
        }
        """;

    mockServer.expect(requestTo(BASE_URL + "/accounts"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.user_id").value("user-uuid-123"))
        .andExpect(jsonPath("$.account_type").value("SAVINGS"))
        .andExpect(jsonPath("$.balance").value(150000.50))
        .andExpect(jsonPath("$.currency").value("COP"))
        .andExpect(jsonPath("$.user_email").value("usuario@ejemplo.com"))
        .andRespond(withStatus(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mockJsonResponse));

    final AccountResponse response = adapter.createAccount(command);

    mockServer.verify();
    assertNotNull(response);
    assertEquals("e3b0c442-98fc-1c14-9afbf4c8996fb924", response.id());
    assertEquals("1002345678", response.accountNumber());
    assertEquals("user-uuid-123", response.userId());
    assertEquals("SAVINGS", response.accountType());
    assertEquals(new BigDecimal("150000.50"), response.balance());
    assertEquals("COP", response.currency());
    assertEquals("ACTIVE", response.status());
    assertEquals(LocalDateTime.parse("2026-10-12T17:30:00"), response.createdAt());
    assertEquals(LocalDateTime.parse("2026-10-12T17:30:00"), response.updatedAt());
  }

  @Test
  @DisplayName("Debe lanzar AccountConflictException cuando el microservicio retorna HTTP 409 Conflict")
  void shouldThrowAccountConflictExceptionWhenRemoteServiceReturns409() {
    final CreateAccountCommand command = CreateAccountCommand.builder()
        .userId("user-uuid-123")
        .accountType(AccountType.SAVINGS)
        .build();

    final String errorResponse = """
        {
          "detail": "Ya existe una cuenta con el número '1002345678' o ID duplicado."
        }
        """;

    mockServer.expect(requestTo(BASE_URL + "/accounts"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.CONFLICT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse));

    final AccountConflictException exception = assertThrows(
        AccountConflictException.class,
        () -> adapter.createAccount(command)
    );

    mockServer.verify();
    assertTrue(exception.getMessage().contains("Ya existe una cuenta"));
  }

  @Test
  @DisplayName("Debe lanzar AccountValidationException cuando el microservicio retorna HTTP 422 Unprocessable Entity")
  void shouldThrowAccountValidationExceptionWhenRemoteServiceReturns422() {
    final CreateAccountCommand command = CreateAccountCommand.builder()
        .userId("user-uuid-123")
        .accountType(AccountType.CHECKING)
        .build();

    final String errorResponse = """
        {
          "detail": "Faltan campos obligatorios o los tipos son inválidos."
        }
        """;

    mockServer.expect(requestTo(BASE_URL + "/accounts"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse));

    final AccountValidationException exception = assertThrows(
        AccountValidationException.class,
        () -> adapter.createAccount(command)
    );

    mockServer.verify();
    assertTrue(exception.getMessage().contains("Faltan campos obligatorios"));
  }

  @Test
  @DisplayName("Debe lanzar AccountServiceException cuando el microservicio retorna HTTP 500 Internal Server Error")
  void shouldThrowAccountServiceExceptionWhenRemoteServiceReturns500() {
    final CreateAccountCommand command = CreateAccountCommand.builder()
        .userId("user-uuid-123")
        .accountType(AccountType.SAVINGS)
        .build();

    final String errorResponse = """
        {
          "detail": "Error al guardar la cuenta en la base de datos: Connection refused"
        }
        """;

    mockServer.expect(requestTo(BASE_URL + "/accounts"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse));

    final AccountServiceException exception = assertThrows(
        AccountServiceException.class,
        () -> adapter.createAccount(command)
    );

    mockServer.verify();
    assertTrue(exception.getMessage().contains("HTTP 500"));
    assertTrue(exception.getMessage().contains("Connection refused"));
  }

  @Test
  @DisplayName("Debe lanzar AccountValidationException si el comando recibido es nulo")
  void shouldThrowAccountValidationExceptionWhenCommandIsNull() {
    assertThrows(AccountValidationException.class, () -> adapter.createAccount(null));
  }
}
