package com.jcaa.usersmanagement.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configuración del cliente HTTP {@link RestClient} para el microservicio externo "Account Microservice".
 *
 * <p>Define la URL base configurable y establece timeouts de conexión y lectura para evitar bloqueos
 * de hilos en la aplicación en caso de lentitud o caída del servicio destino.
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class AccountClientConfig {

  @Value("${account.service.url:http://localhost:8000}")
  private String accountServiceUrl;

  @Value("${account.service.connect-timeout-seconds:5}")
  private int connectTimeoutSeconds;

  @Value("${account.service.read-timeout-seconds:10}")
  private int readTimeoutSeconds;

  @Bean
  public RestClient accountRestClient(final RestClient.Builder restClientBuilder) {
    log.info("[AccountClientConfig] Inicializando RestClient para Account Microservice en: {} "
            + "(Connect Timeout: {}s, Read Timeout: {}s)",
        accountServiceUrl, connectTimeoutSeconds, readTimeoutSeconds);

    final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds));
    requestFactory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));

    return restClientBuilder
        .baseUrl(accountServiceUrl)
        .requestFactory(requestFactory)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
