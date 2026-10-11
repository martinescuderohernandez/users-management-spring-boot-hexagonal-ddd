package com.jcaa.usersmanagement.infrastructure.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration(proxyBeanMethods = false)
public class AsyncNotificationConfig {

  private static final int SHUTDOWN_WAIT_SECONDS = 30;

  /**
   * Executor dedicado para publicación de eventos a Kafka.
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

