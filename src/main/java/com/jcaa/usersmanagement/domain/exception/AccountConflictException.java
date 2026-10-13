package com.jcaa.usersmanagement.domain.exception;

/**
 * Representa un conflicto (HTTP 409) al intentar crear una cuenta en el microservicio externo
 * (ej. número de cuenta duplicado o ID duplicado).
 */
public final class AccountConflictException extends AccountIntegrationException {

  private AccountConflictException(final String message) {
    super(message);
  }

  public static AccountConflictException of(final String detail) {
    return new AccountConflictException(
        detail != null && !detail.isBlank() ? detail : "Conflicto por número de cuenta o ID duplicado.");
  }
}
