package com.smogian.user.registration.config;

import com.smogian.user.registration.controller.UserRegisterValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class AwsConfiguration {

  @Bean
  public LocalValidatorFactoryBean localValidatorFactoryBean() {
    return new LocalValidatorFactoryBean();
  }

  @Profile("stage")
  @Bean
  public SqsAsyncClient sqsAsyncClient() {
    return SqsAsyncClient.builder().region(Region.CA_CENTRAL_1).build();
  }

  @Bean
  public UserRegisterValidator userRegisterValidator(final LocalValidatorFactoryBean localValidatorFactoryBean) {
    return new UserRegisterValidator(localValidatorFactoryBean);
  }
}
