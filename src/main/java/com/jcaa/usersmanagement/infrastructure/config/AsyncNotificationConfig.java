package com.jcaa.usersmanagement.infrastructure.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration(proxyBeanMethods = false)
public class AsyncNotificationConfig {

  private static final int CORE_POOL_SIZE = 2;
  private static final int MAX_POOL_SIZE = 4;
  private static final int QUEUE_CAPACITY = 100;
  private static final int SHUTDOWN_WAIT_SECONDS = 30;

  @Bean(name = "notificationExecutor")
  public Executor notificationExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(CORE_POOL_SIZE);
    executor.setMaxPoolSize(MAX_POOL_SIZE);
    executor.setQueueCapacity(QUEUE_CAPACITY);
    executor.setThreadNamePrefix("email-notification-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(SHUTDOWN_WAIT_SECONDS);
    executor.initialize();
    return executor;
  }

  /**
   * Executor dedicado para publicación de eventos a Kafka.
   * Pool separado del de emails para evitar contención entre ambas tareas asíncronas.
   */
  @Bean(name = "kafkaPublisherExecutor")
  public Executor kafkaPublisherExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(200);
    executor.setThreadNamePrefix("kafka-publisher-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(SHUTDOWN_WAIT_SECONDS);
    executor.initialize();
    return executor;
  }
}

