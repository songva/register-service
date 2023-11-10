package com.smogian.user.registration.controller;

import com.smogian.user.registration.domain.User;
import jakarta.validation.ValidationException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

@Tag("integration")
@SpringBootTest
class UserRegisterValidatorIntegrationTest {
  @Autowired
  private LocalValidatorFactoryBean localValidatorFactoryBean;

  private ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

  private UserRegisterValidator userRegisterValidator;

  @BeforeEach
  void setup() {
    userRegisterValidator = new UserRegisterValidator(localValidatorFactoryBean);
  }

  @Test
  void givenUserWithLastNameOnly_whenValidatingUser_shouldThrowExceptionWithMessages() {
    //given
    val nathan = User.builder().lastName("Rich").build();

    // when
    val thrown = catchThrowable(() -> userRegisterValidator.validate(nathan));

    // then
    assertThat(thrown)
            .isInstanceOf(ValidationException.class)
            .hasMessageContainingAll(
                    "firstName must not be empty",
                    "email must not be empty",
                    "password must not be empty");
  }

  @Test
  void givenPasswordDoesNotMeetComplicity_whenValidatingUser_shouldThrowExceptionWithPasswordCriterion() {
    //given
    val nathan = User.builder()
            .firstName("Nathan")
            .lastName("Rich")
            .email("nathan.rich@somgian.com")
            .password("too simple")
            .build();

    // when
    val thrown = catchThrowable(() -> userRegisterValidator.validate(nathan));

    // then
    assertThat(thrown)
            .isInstanceOf(ValidationException.class)
            .hasMessage("password must contain at least one digit, one lowercase letter, one uppercase letter, " +
                    "one special character, and between 8 characters and 20 characters");
  }

  @Test
  void givenValidUser_whenValidatingUser_shouldReturnTheUserItself() {
    //given
    val nathan = User.builder()
            .firstName("Nathan")
            .lastName("Rich")
            .email("nathan.rich@somgian.com")
            .password("nGli7y{AW-MC")
            .build();

    // when
    val user = userRegisterValidator.validate(nathan);

    // then
    assertThat(user).isEqualTo(nathan);
  }
}