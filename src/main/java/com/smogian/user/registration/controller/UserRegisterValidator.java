package com.smogian.user.registration.controller;

import com.smogian.user.registration.domain.User;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UserRegisterValidator {
  private final LocalValidatorFactoryBean localValidatorFactoryBean;

  public User validate(final User user) {
    Optional.of(localValidatorFactoryBean.getValidator().validate(user))
            .filter(Predicate.not(CollectionUtils::isEmpty))
            .ifPresent(errors -> {
              throw new ValidationException(
                      errors.stream()
                              .map(violation -> String.join(
                                      " ",
                                      violation.getPropertyPath().toString(),
                                      violation.getMessage()))
                              .collect(Collectors.joining("; ")));
            });
    return user;
  }
}
