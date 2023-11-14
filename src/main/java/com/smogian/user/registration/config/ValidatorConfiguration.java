package com.smogian.user.registration.config;

import com.smogian.user.registration.controller.UserRegisterValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidatorConfiguration {
  @Bean
  public LocalValidatorFactoryBean localValidatorFactoryBean() {
    return new LocalValidatorFactoryBean();
  }

  @Bean
  public UserRegisterValidator userRegisterValidator(final LocalValidatorFactoryBean localValidatorFactoryBean) {
    return new UserRegisterValidator(localValidatorFactoryBean);
  }
}
