package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper;

import com.jcaa.usersmanagement.application.service.dto.command.CreateAccountCommand;
import com.jcaa.usersmanagement.application.service.dto.response.AccountResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.CreateAccountRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.AccountRestResponse;

public final class AccountRestMapper {

  private AccountRestMapper() {}

  public static CreateAccountCommand toCommand(final CreateAccountRestRequest request) {
    if (request == null) {
      return null;
    }
    return CreateAccountCommand.builder()
        .userId(request.userId())
        .accountType(request.accountType())
        .balance(request.balance())
        .currency(request.currency())
        .accountNumber(request.accountNumber())
        .userEmail(request.userEmail())
        .build();
  }

  public static AccountRestResponse toResponse(final AccountResponse response) {
    if (response == null) {
      return null;
    }
    return new AccountRestResponse(
        response.id(),
        response.accountNumber(),
        response.userId(),
        response.accountType(),
        response.balance(),
        response.currency(),
        response.status(),
        response.createdAt(),
        response.updatedAt()
    );
  }
}
