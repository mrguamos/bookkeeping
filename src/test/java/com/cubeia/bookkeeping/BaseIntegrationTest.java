package com.cubeia.bookkeeping;

import java.time.Duration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.4")
    .withDatabaseName("bookkeeping")
    .withMinimumRunningDuration(Duration.ofSeconds(5L));

  static {
    postgres.start();
  }

} 