package com.smogian.user.registration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.BDDAssertions.thenCode;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("stage")
class RegistrationApplicationIntegrationTest {

  @Test
  void whenApplicationLaunched_shouldNoExceptionBeenThrown() {
    thenCode(() -> RegistrationApplication.main(new String[]{})).doesNotThrowAnyException();
  }
}