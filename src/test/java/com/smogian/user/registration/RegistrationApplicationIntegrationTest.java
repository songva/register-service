package com.smogian.user.registration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.thenCode;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("stage")
class RegistrationApplicationIntegrationTest {
  @Autowired
  private ApplicationContext applicationContext;

  @Test
  void whenApplicationLaunched_shouldNoExceptionBeenThrown() {
    thenCode(() -> RegistrationApplication.main(new String[]{})).doesNotThrowAnyException();
  }

  @Test
  void whenApplicationLaunched_shouldBeansBeenRegistered() {
    assertThat(applicationContext.getBean("register")).isNotNull();
  }
}