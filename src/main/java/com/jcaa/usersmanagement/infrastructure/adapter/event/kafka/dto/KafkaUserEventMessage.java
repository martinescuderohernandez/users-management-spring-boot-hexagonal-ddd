package com.jcaa.usersmanagement.infrastructure.adapter.event.kafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;

/**
 * DTO que representa el envelope (sobre) del mensaje que se publica en Kafka.
 *
 * <p>Estructura JSON publicada en {@code user-created-topic}, {@code user-updated-topic} y {@code user-deleted-topic}:
 * <pre>{@code
 * {
 *   "eventId":       "uuid-v4",
 *   "eventType":     "user.created | user.updated | user.deleted",
 *   "aggregateType": "USER",
 *   "aggregateId":   "userId-uuid",
 *   "timestamp":     "2026-10-11T14:45:00Z",
 *   "version":       "1.0",
 *   "producer":      "users-management-service",
 *   "data":          { ... }
 * }
 * }</pre>
 */
public record KafkaUserEventMessage(

    @JsonProperty("eventId")
    String eventId,

    @JsonProperty("eventType")
    String eventType,

    @JsonProperty("aggregateType")
    String aggregateType,

    @JsonProperty("aggregateId")
    String aggregateId,

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant timestamp,

    @JsonProperty("version")
    String version,

    @JsonProperty("producer")
    String producer,

    @JsonProperty("data")
    Map<String, String> data
) {
  private static final String AGGREGATE_TYPE = "USER";
  private static final String SCHEMA_VERSION = "1.0";
  private static final String PRODUCER_NAME  = "users-management-service";

  /**
   * Factory method que construye el mensaje a partir de los campos del evento de dominio.
   */
  public static KafkaUserEventMessage of(
      final String eventId,
      final String eventType,
      final String aggregateId,
      final Instant timestamp,
      final Map<String, String> data) {

    return new KafkaUserEventMessage(
        eventId,
        eventType,
        AGGREGATE_TYPE,
        aggregateId,
        timestamp,
        SCHEMA_VERSION,
        PRODUCER_NAME,
        data);
  }
}
