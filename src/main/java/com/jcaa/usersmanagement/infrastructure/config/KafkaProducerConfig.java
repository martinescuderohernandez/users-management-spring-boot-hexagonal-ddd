package com.jcaa.usersmanagement.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jcaa.usersmanagement.infrastructure.adapter.event.kafka.dto.KafkaUserEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración manual del producer de Kafka para Aiven (SSL/PKCS12).
 *
 * <p>Sigue el mismo patrón de wiring manual que {@link DataSourceSpringConfig}:
 * inyecta propiedades mediante {@code @Value} y construye los beans explícitamente,
 * sin depender de la auto-configuración de Spring Boot para Kafka.
 *
 * <p>La auto-configuración de Kafka se excluye en {@code application.properties}
 * ({@code spring.autoconfigure.exclude[3]=...KafkaAutoConfiguration}).
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class KafkaProducerConfig {

  private static final String LOG_INIT =
      "[KafkaProducerConfig] KafkaTemplate inicializado. bootstrapServers={}";

  @Value("${kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${kafka.producer.client-id}")
  private String clientId;

  @Value("${kafka.ssl.truststore.location}")
  private Resource truststoreResource;

  @Value("${kafka.ssl.truststore.password}")
  private String truststorePassword;

  @Value("${kafka.ssl.truststore.type}")
  private String truststoreType;

  @Value("${kafka.ssl.keystore.location}")
  private Resource keystoreResource;

  @Value("${kafka.ssl.keystore.password}")
  private String keystorePassword;

  @Value("${kafka.ssl.keystore.type}")
  private String keystoreType;

  @Value("${kafka.ssl.key.password}")
  private String keyPassword;

  @Bean
  public ProducerFactory<String, KafkaUserEventMessage> kafkaProducerFactory() throws IOException {
    final Map<String, Object> props = new HashMap<>();

    // --- Conexión ---
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);

    // --- Fiabilidad ---
    props.put(ProducerConfig.ACKS_CONFIG, "all");               // todos los in-sync replicas confirman
    props.put(ProducerConfig.RETRIES_CONFIG, 3);
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);  // exactly-once dentro del producer

    // --- SSL (Aiven) ---
    // Los .p12 están dentro del JAR (classpath). Kafka espera rutas de sistema de archivos,
    // así que los extraemos a un directorio temporal al arrancar.
    props.put("security.protocol", "SSL");
    props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, extractToTempFile(truststoreResource, "truststore.p12"));
    props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
    props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, truststoreType);
    props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, extractToTempFile(keystoreResource, "keystore.p12"));
    props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
    props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, keystoreType);
    props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, keyPassword);

    final DefaultKafkaProducerFactory<String, KafkaUserEventMessage> factory =
        new DefaultKafkaProducerFactory<>(
            props,
            new StringSerializer(),
            buildJsonSerializer()
        );

    log.info(LOG_INIT, bootstrapServers);
    return factory;
  }

  @Bean
  public KafkaTemplate<String, KafkaUserEventMessage> kafkaTemplate(
      final ProducerFactory<String, KafkaUserEventMessage> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  /**
   * Extrae un recurso de classpath a un archivo temporal en el sistema de archivos
   * para que la librería Kafka (que no entiende classpath://) pueda acceder a él.
   */
  private String extractToTempFile(final Resource resource, final String filename) throws IOException {
    final Path tempDir = Files.createTempDirectory("kafka-ssl-");
    final Path target  = tempDir.resolve(filename);
    if (resource != null && resource.exists()) {
      Files.copy(resource.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
      log.debug("[KafkaProducerConfig] Certificado SSL extraído: {}", target.toAbsolutePath());
    } else {
      log.warn("[KafkaProducerConfig] Certificado SSL ({}) no encontrado en classpath. Creando archivo temporal vacío para permitir arranque local.", filename);
      Files.createFile(target);
    }
    target.toFile().deleteOnExit();
    return target.toAbsolutePath().toString();
  }

  /**
   * Construye un {@link JsonSerializer} con soporte para {@link java.time.Instant}
   * (módulo JSR-310 de Jackson) y sin cabeceras de tipo propietarias de Spring.
   */
  private JsonSerializer<KafkaUserEventMessage> buildJsonSerializer() {
    final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    final JsonSerializer<KafkaUserEventMessage> serializer = new JsonSerializer<>(mapper);
    serializer.setAddTypeInfo(false);
    return serializer;
  }
}
