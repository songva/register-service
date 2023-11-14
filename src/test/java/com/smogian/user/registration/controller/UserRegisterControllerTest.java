package com.smogian.user.registration.controller;

import com.smogian.user.registration.domain.User;
import jakarta.validation.ValidationException;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserRegisterControllerTest {
  private static MockedStatic<MessageBuilder> mockedMessageBuilder;
  @Mock
  private StreamBridge streamBridge;
  @Mock
  private UserRegisterValidator userRegisterValidator;

  private UserRegisterController userRegisterController;

  @BeforeAll
  static void init() {
    mockedMessageBuilder = Mockito.mockStatic(MessageBuilder.class, RETURNS_DEEP_STUBS);
    mockedMessageBuilder.when(() -> MessageBuilder.withPayload(any(User.class)).build())
            .thenReturn(new GenericMessage<>("general message"));
  }

  @AfterAll
  static void clean() {
    mockedMessageBuilder.close();
  }

  @BeforeEach
  void setup() {
    userRegisterController = new UserRegisterController(streamBridge, userRegisterValidator);
  }

  @Test
  void givenInvalidUser_whenRegister_shouldReturnWithError() {
    // given
    val user = User.builder().build();
    when(userRegisterValidator.validate(user)).thenThrow(new ValidationException("validation error"));

    // when
    val source = userRegisterController.register().apply(Mono.just(user));

    // then
    StepVerifier.create(source)
            .expectNext(
                    ResponseEntity
                            .badRequest()
                            .body("validation error"))
            .verifyComplete();
  }

  @Test
  void givenUnknownMessageDeliveryIssueOccurs_whenRegister_shouldReturnWithError() {
    // given
    val user = User.builder().build();
    when(userRegisterValidator.validate(user)).thenReturn(user);
    when(streamBridge.send(anyString(), any(GenericMessage.class))).thenReturn(false);

    // when
    val source = userRegisterController.register().apply(Mono.just(user));

    // then
    StepVerifier.create(source)
            .expectNext(
                    ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("failed to send message"))
            .verifyComplete();
  }

  @Test
  void givenUnknownQueueConfigured_whenRegister_shouldReturnWithError() {
    // given
    val user = User.builder().build();
    when(userRegisterValidator.validate(user)).thenReturn(user);
    when(streamBridge.send(anyString(), any(GenericMessage.class)))
            .thenThrow(new MessageHandlingException(new GenericMessage<>(""), "unknown queue configured"));

    // when
    val source = userRegisterController.register().apply(Mono.just(user));

    // then
    StepVerifier.create(source)
            .expectNext(
                    ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("unknown queue configured"))
            .verifyComplete();
  }

  @Test
  void givenValidUser_whenRegister_shouldReturnEmptyList() {
    // given
    val user = User.builder().build();
    when(userRegisterValidator.validate(user)).thenReturn(user);
    when(streamBridge.send(anyString(), any(GenericMessage.class))).thenReturn(true);

    // when
    val source = userRegisterController.register().apply(Mono.just(user));

    // then

    StepVerifier.create(source)
            .expectNext(ResponseEntity.ok().build())
            .verifyComplete();
  }
}