package com.jcaa.usersmanagement.domain.exception;

/**
 * Representa un error de validación o entidad no procesable (HTTP 422) retornado por el microservicio externo.
 */
public final class AccountValidationException extends AccountIntegrationException {

  private AccountValidationException(final String message) {
    super(message);
  }

  public static AccountValidationException of(final String detail) {
    return new AccountValidationException(
        detail != null && !detail.isBlank() ? detail : "Faltan campos obligatorios o los tipos de datos son inválidos.");
  }
}
