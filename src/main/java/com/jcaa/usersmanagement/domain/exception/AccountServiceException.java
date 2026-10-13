package com.jcaa.usersmanagement.domain.exception;

/**
 * Representa errores internos del servidor (HTTP 500), fallos de conexión o respuestas anómalas
 * generadas por el microservicio externo "Account Microservice".
 */
public final class AccountServiceException extends AccountIntegrationException {

  private AccountServiceException(final String message) {
    super(message);
  }

  private AccountServiceException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public static AccountServiceException becauseRemoteServerError(final int statusCode, final String detail) {
    return new AccountServiceException(
        String.format("Error interno en Account Microservice (HTTP %d): %s", statusCode, detail));
  }

  public static AccountServiceException becauseClientError(final int statusCode, final String detail) {
    return new AccountServiceException(
        String.format("Error de cliente no esperado en Account Microservice (HTTP %d): %s", statusCode, detail));
  }

  public static AccountServiceException becauseConnectionFailed(final String message, final Throwable cause) {
    return new AccountServiceException(message, cause);
  }

  public static AccountServiceException becauseUnexpectedError(final String message, final Throwable cause) {
    return new AccountServiceException(message, cause);
  }

  public static AccountServiceException becauseNullResponse() {
    return new AccountServiceException("El microservicio de cuentas retornó una respuesta nula.");
  }
}
