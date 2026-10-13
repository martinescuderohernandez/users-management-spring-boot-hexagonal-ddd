package com.jcaa.usersmanagement.domain.exception;

/**
 * Representa el agotamiento del tiempo de espera (Timeout) durante la comunicación síncrona
 * con el microservicio externo "Account Microservice".
 */
public final class AccountTimeoutException extends AccountIntegrationException {

  private AccountTimeoutException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public static AccountTimeoutException of(final String message, final Throwable cause) {
    return new AccountTimeoutException(message, cause);
  }
}
