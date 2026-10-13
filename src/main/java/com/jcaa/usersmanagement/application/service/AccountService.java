package com.jcaa.usersmanagement.application.service;

import com.jcaa.usersmanagement.application.port.in.CreateAccountUseCase;
import com.jcaa.usersmanagement.application.port.out.AccountClientPort;
import com.jcaa.usersmanagement.application.service.dto.command.CreateAccountCommand;
import com.jcaa.usersmanagement.application.service.dto.response.AccountResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Servicio de aplicación dedicado a orquestar y validar la creación de cuentas en el microservicio externo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService implements CreateAccountUseCase {

  private final AccountClientPort accountClientPort;
  private final Validator validator;

  @Override
  public AccountResponse execute(final CreateAccountCommand command) {
    validateCommand(command);
    log.info("[AccountService] Iniciando creación de cuenta para usuario: {}", command.userId());
    return accountClientPort.createAccount(command);
  }

  private void validateCommand(final CreateAccountCommand command) {
    if (command == null) {
      throw new IllegalArgumentException("El comando CreateAccountCommand no puede ser nulo.");
    }
    final Set<ConstraintViolation<CreateAccountCommand>> violations = validator.validate(command);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
  }
}
