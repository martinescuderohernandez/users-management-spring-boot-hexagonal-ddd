package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.CreateAccountRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.AccountRestResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Contrato OpenAPI / Swagger para {@link AccountRestController}.
 */
@Tag(name = "Accounts", description = "Integración síncrona con el microservicio de cuentas.")
public interface AccountRestControllerDocs {

  @Operation(
      summary = "Crear cuenta bancaria (síncrono)",
      description = "Invoca de forma síncrona el microservicio externo de cuentas para registrar una cuenta asociada a un usuario.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Cuenta creada exitosamente en el microservicio externo.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AccountRestResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Datos de entrada inválidos (campos requeridos faltantes o valores incorrectos).",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Conflicto: número de cuenta o usuario ya existente.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(
        responseCode = "422",
        description = "Entidad no procesable: validación de negocio fallida en el microservicio externo.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(
        responseCode = "502",
        description = "Bad Gateway: error en la comunicación con el microservicio de cuentas.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
    @ApiResponse(
        responseCode = "504",
        description = "Gateway Timeout: tiempo de espera agotado al conectar con el microservicio de cuentas.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  AccountRestResponse create(@Valid @RequestBody CreateAccountRestRequest request);
}
