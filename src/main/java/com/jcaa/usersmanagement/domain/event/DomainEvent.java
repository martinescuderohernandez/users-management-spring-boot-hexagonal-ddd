package com.jcaa.usersmanagement.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

/**
 * Clase base de todos los eventos de dominio.
 *
 * <p>Cada evento lleva una cabecera fija (envelope) que permite al consumidor (Notifiers)
 * identificar y enrutar el mensaje sin necesidad de deserializar el payload completo:
 * <ul>
 *   <li>{@code eventId}     — UUID único para idempotencia en el consumer.</li>
 *   <li>{@code eventType}   — nombre del evento (ej. {@code user.created}).</li>
 *   <li>{@code aggregateId} — ID del agregado raíz afectado (ej. userId).</li>
 *   <li>{@code occurredOn}  — instante en que ocurrió el evento (UTC, ISO-8601).</li>
 * </ul>
 */
@Getter
public abstract class DomainEvent {

  private final String eventId;
  private final String eventType;
  private final String aggregateId;
  private final Instant occurredOn;

  protected DomainEvent(final String eventType, final String aggregateId) {
    this.eventId     = UUID.randomUUID().toString();
    this.eventType   = eventType;
    this.aggregateId = aggregateId;
    this.occurredOn  = Instant.now();
  }

  /**
   * Alias de conveniencia para compatibilidad con código que usa getEventName().
   */
  public String getEventName() {
    return this.eventType;
  }

  /**
   * Devuelve el bloque {@code data} del mensaje Kafka como pares clave-valor.
   * Cada subclase define qué campos expone.
   */
  public abstract Map<String, String> payload();
}
