package com.jcaa.usersmanagement.infrastructure.adapter.event.kafka;

import com.jcaa.usersmanagement.application.port.out.UserEventPublisherPort;
import com.jcaa.usersmanagement.domain.event.DomainEvent;
import com.jcaa.usersmanagement.infrastructure.adapter.event.kafka.dto.KafkaUserEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Adaptador de salida (driven adapter) que implementa {@link UserEventPublisherPort}
 * publicando los eventos de dominio en Kafka / Aiven.
 *
 * <p><strong>Partición</strong>: se usa el {@code aggregateId} (userId) como clave del mensaje
 * para garantizar que todos los eventos de un mismo usuario lleguen a la misma partición
 * y en orden cronológico estricto.
 *
 * <p><strong>Asincronía</strong>: la publicación se hace en el executor {@code kafkaPublisherExecutor}
 * (pool independiente del {@code notificationExecutor} de emails) para no bloquear el hilo HTTP.
 * El commit a la BD ya ocurrió antes de que se invoque este método.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUserEventPublisherAdapter implements UserEventPublisherPort {

  private static final String LOG_PUBLISHED =
      "[KafkaPublisher] Evento publicado — topic={} key={} eventType={} eventId={}";
  private static final String LOG_FAILED =
      "[KafkaPublisher] Error al publicar evento — topic={} eventType={} eventId={} causa={}";

  private final KafkaTemplate<String, KafkaUserEventMessage> kafkaTemplate;

  @Value("${kafka.topic.users}")
  private String usersTopic;

  @Async("kafkaPublisherExecutor")
  @Override
  public void publish(final DomainEvent event) {
    final KafkaUserEventMessage message = KafkaUserEventMessage.of(
        event.getEventId(),
        event.getEventType(),
        event.getAggregateId(),
        event.getOccurredOn(),
        event.payload());

    // La clave del ProducerRecord = aggregateId (userId) → garantiza orden por usuario
    final ProducerRecord<String, KafkaUserEventMessage> record =
        new ProducerRecord<>(usersTopic, event.getAggregateId(), message);

    final CompletableFuture<SendResult<String, KafkaUserEventMessage>> future =
        kafkaTemplate.send(record);

    future.whenComplete((result, ex) -> {
      if (ex != null) {
        log.error(LOG_FAILED, usersTopic, event.getEventType(), event.getEventId(), ex.getMessage(), ex);
      } else {
        log.info(LOG_PUBLISHED,
            result.getRecordMetadata().topic(),
            event.getAggregateId(),
            event.getEventType(),
            event.getEventId());
      }
    });
  }
}
