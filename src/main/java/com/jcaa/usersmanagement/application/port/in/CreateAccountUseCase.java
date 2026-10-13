package com.jcaa.usersmanagement.application.port.in;

import com.jcaa.usersmanagement.application.service.dto.command.CreateAccountCommand;
import com.jcaa.usersmanagement.application.service.dto.response.AccountResponse;

/**
 * Puerto de entrada (driving port) para el caso de uso de creación de cuentas de usuario.
 */
public interface CreateAccountUseCase {

  /**
   * Ejecuta el flujo de creación de cuenta integrándose síncronamente con el microservicio externo.
   *
   * @param command datos para la creación de la cuenta; nunca {@code null}.
   * @return información de la cuenta creada.
   */
  AccountResponse execute(CreateAccountCommand command);
}
