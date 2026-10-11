package com.jcaa.usersmanagement.infrastructure.adapter.event.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.enums.UserStatus;
import com.jcaa.usersmanagement.domain.event.DomainEvent;
import com.jcaa.usersmanagement.domain.event.UserCreatedDomainEvent;
import com.jcaa.usersmanagement.domain.event.UserDeletedDomainEvent;
import com.jcaa.usersmanagement.domain.event.UserUpdatedDomainEvent;
import com.jcaa.usersmanagement.domain.model.UserModel;
import com.jcaa.usersmanagement.domain.valueobject.UserEmail;
import com.jcaa.usersmanagement.domain.valueobject.UserId;
import com.jcaa.usersmanagement.domain.valueobject.UserName;
import com.jcaa.usersmanagement.domain.valueobject.UserPassword;
import com.jcaa.usersmanagement.infrastructure.adapter.event.kafka.dto.KafkaUserEventMessage;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("KafkaUserEventPublisherAdapter")
@ExtendWith(MockitoExtension.class)
class KafkaUserEventPublisherAdapterTest {

  @Mock
  private KafkaTemplate<String, KafkaUserEventMessage> kafkaTemplate;

  @Captor
  private ArgumentCaptor<ProducerRecord<String, KafkaUserEventMessage>> recordCaptor;

  private KafkaUserEventPublisherAdapter adapter;

  private UserModel user;

  @BeforeEach
  void setUp() {
    adapter = new KafkaUserEventPublisherAdapter(kafkaTemplate);
    ReflectionTestUtils.setField(adapter, "userCreatedTopic", "user-created-topic");
    ReflectionTestUtils.setField(adapter, "userUpdatedTopic", "user-updated-topic");
    ReflectionTestUtils.setField(adapter, "userDeletedTopic", "user-deleted-topic");

    user = new UserModel(
        new UserId("usr-123"),
        new UserName("Test User"),
        new UserEmail("test@example.com"),
        UserPassword.fromPlainText("Secret123"),
        UserRole.MEMBER,
        UserStatus.ACTIVE);

    when(kafkaTemplate.send(any(ProducerRecord.class)))
        .thenReturn(new CompletableFuture<>());
  }

  @Test
  @DisplayName("publish(UserCreatedDomainEvent) debe enviar al tópico 'user-created-topic'")
  void shouldPublishToUserCreatedTopic() {
    final UserCreatedDomainEvent event = new UserCreatedDomainEvent(user);

    adapter.publish(event);

    verify(kafkaTemplate).send(recordCaptor.capture());
    final ProducerRecord<String, KafkaUserEventMessage> record = recordCaptor.getValue();
    assertEquals("user-created-topic", record.topic());
    assertEquals("usr-123", record.key());
    assertEquals("user.created", record.value().eventType());
  }

  @Test
  @DisplayName("publish(UserUpdatedDomainEvent) debe enviar al tópico 'user-updated-topic'")
  void shouldPublishToUserUpdatedTopic() {
    final UserUpdatedDomainEvent event = new UserUpdatedDomainEvent(user);

    adapter.publish(event);

    verify(kafkaTemplate).send(recordCaptor.capture());
    final ProducerRecord<String, KafkaUserEventMessage> record = recordCaptor.getValue();
    assertEquals("user-updated-topic", record.topic());
    assertEquals("usr-123", record.key());
    assertEquals("user.updated", record.value().eventType());
  }

  @Test
  @DisplayName("publish(UserDeletedDomainEvent) debe enviar al tópico 'user-deleted-topic'")
  void shouldPublishToUserDeletedTopic() {
    final UserDeletedDomainEvent event = new UserDeletedDomainEvent(new UserId("usr-123"));

    adapter.publish(event);

    verify(kafkaTemplate).send(recordCaptor.capture());
    final ProducerRecord<String, KafkaUserEventMessage> record = recordCaptor.getValue();
    assertEquals("user-deleted-topic", record.topic());
    assertEquals("usr-123", record.key());
    assertEquals("user.deleted", record.value().eventType());
  }

  @Test
  @DisplayName("publish con un evento desconocido debe lanzar IllegalArgumentException")
  void shouldThrowWhenUnknownEventType() {
    final DomainEvent unknownEvent = new DomainEvent("unknown.type", "usr-123") {
      @Override
      public Map<String, String> payload() {
        return Map.of();
      }
    };

    assertThrows(IllegalArgumentException.class, () -> adapter.publish(unknownEvent));
  }
}
