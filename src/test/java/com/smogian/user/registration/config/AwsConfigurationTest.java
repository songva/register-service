package com.smogian.user.registration.config;

import com.smogian.user.registration.controller.UserRegisterValidator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("stage")
class AwsConfigurationTest {
  @Autowired
  private ApplicationContext applicationContext;

  @Test
  void whenApplicationLaunched_shouldBeansBeenRegistered() {
    assertSoftly(softly -> {
      softly.assertThat(applicationContext.getBean(SqsAsyncClient.class))
              .isInstanceOf(SqsAsyncClient.class);
      softly.assertThat(applicationContext.getBean(UserRegisterValidator.class))
              .isInstanceOf(UserRegisterValidator.class);
      softly.assertThat(applicationContext.getBean(LocalValidatorFactoryBean.class))
              .isInstanceOf(LocalValidatorFactoryBean.class);
      softly.assertThat(applicationContext.getBean(StreamBridge.class))
              .isInstanceOf(StreamBridge.class);
    });
  }

}
