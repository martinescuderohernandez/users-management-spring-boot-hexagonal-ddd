package com.jcaa.usersmanagement.application.port.out;

import com.jcaa.usersmanagement.domain.event.DomainEvent;

/**
 * Puerto de salida (driven port) para publicar eventos de dominio de usuarios
 * de forma asíncrona a un broker de mensajería (Kafka / Aiven).
 *
 * <p>La implementación concreta vive en la capa de infraestructura
 * ({@code KafkaUserEventPublisherAdapter}) y es invisible para el dominio y la aplicación.
 */
public interface UserEventPublisherPort {

  /**
   * Publica un evento de dominio al tópico correspondiente.
   *
   * @param event el evento de dominio a publicar; nunca {@code null}.
   */
  void publish(DomainEvent event);
}
