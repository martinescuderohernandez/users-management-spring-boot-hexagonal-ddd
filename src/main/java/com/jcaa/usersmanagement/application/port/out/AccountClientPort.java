package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.application.service.dto.command.CreateAccountCommand;
import com.jcaa.usersmanagement.application.service.dto.response.AccountResponse;

/**
 * Puerto de salida (driven port) para la integración síncrona con el microservicio externo "Account Microservice".
 *
 * <p>La implementación concreta reside en la capa de infraestructura
 * ({@code AccountRestClientAdapter}), desacoplando la lógica de negocio y aplicación del protocolo HTTP.
 */
public interface AccountClientPort {

  /**
   * Ejecuta la solicitud síncrona HTTP POST hacia {@code /accounts} en el microservicio externo.
   *
   * @param command datos necesarios para la creación de la cuenta; nunca {@code null}.
   * @return información de la cuenta creada tras recibir respuesta HTTP 201 Created.
   */
  AccountResponse createAccount(CreateAccountCommand command);
}
