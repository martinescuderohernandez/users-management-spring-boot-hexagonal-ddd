package com.jcaa.usersmanagement.domain.exception;

/**
 * Excepción base para errores ocurridos durante la integración con el microservicio externo de cuentas.
 */
public abstract class AccountIntegrationException extends DomainException {

  protected AccountIntegrationException(final String message) {
    super(message);
  }

  protected AccountIntegrationException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
