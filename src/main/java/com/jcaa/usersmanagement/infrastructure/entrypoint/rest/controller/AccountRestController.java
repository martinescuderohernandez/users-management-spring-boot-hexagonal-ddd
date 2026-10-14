package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.controller;

import com.jcaa.usersmanagement.application.port.in.CreateAccountUseCase;
import com.jcaa.usersmanagement.application.service.dto.command.CreateAccountCommand;
import com.jcaa.usersmanagement.application.service.dto.response.AccountResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.request.CreateAccountRestRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.AccountRestResponse;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.mapper.AccountRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para invocar sincrónicamente la creación de cuentas en el microservicio externo.
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountRestController implements AccountRestControllerDocs {

  private final CreateAccountUseCase createAccountUseCase;

  @Override
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AccountRestResponse create(@Valid @RequestBody final CreateAccountRestRequest request) {
    log.info("[AccountRestController] Petición REST recibida para crear cuenta de usuario: {}", request.userId());
    final CreateAccountCommand command = AccountRestMapper.toCommand(request);
    final AccountResponse response = createAccountUseCase.execute(command);
    return AccountRestMapper.toResponse(response);
  }
}
