package com.jcaa.usersmanagement.infrastructure.adapter.client.account.mapper;

import com.jcaa.usersmanagement.application.service.dto.command.CreateAccountCommand;
import com.jcaa.usersmanagement.application.service.dto.response.AccountResponse;
import com.jcaa.usersmanagement.infrastructure.adapter.client.account.dto.AccountCreationRestRequest;
import com.jcaa.usersmanagement.infrastructure.adapter.client.account.dto.AccountRestResponse;

/**
 * Mapper para convertir entre objetos de aplicación (comandos/respuestas) y DTOs de infraestructura del cliente REST.
 */
public final class AccountClientMapper {

  private AccountClientMapper() {}

  public static AccountCreationRestRequest toRestRequest(final CreateAccountCommand command) {
    if (command == null) {
      return null;
    }
    return new AccountCreationRestRequest(
        command.userId(),
        command.accountType() != null ? command.accountType().name() : null,
        command.balance(),
        command.currency(),
        command.accountNumber(),
        command.userEmail()
    );
  }

  public static AccountResponse toResponse(final AccountRestResponse restResponse) {
    if (restResponse == null) {
      return null;
    }
    return new AccountResponse(
        restResponse.id(),
        restResponse.accountNumber(),
        restResponse.userId(),
        restResponse.accountType(),
        restResponse.balance(),
        restResponse.currency(),
        restResponse.status(),
        restResponse.createdAt(),
        restResponse.updatedAt()
    );
  }
}
