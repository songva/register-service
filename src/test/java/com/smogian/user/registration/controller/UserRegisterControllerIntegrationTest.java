package com.smogian.user.registration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smogian.user.registration.domain.User;
import com.smogian.user.registration.utils.LocalstackTestConfiguration;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("integration")
@Import(LocalstackTestConfiguration.class)
@SpringBootTest
class UserRegisterControllerIntegrationTest {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ArgumentCaptor<Message<String>> messageCaptor = ArgumentCaptor.forClass(Message.class);
  private final Random random = new Random();

  @Autowired
  private UserRegisterValidator userRegisterValidator;

  private UserRegisterController userRegisterController;

  @Nested
  @ActiveProfiles(profiles = "test")
  class ValidQueueProfile {
    @Value("${spring.cloud.stream.bindings.userRegister.destination}")
    private String userRegisterQueue;
    @Autowired
    private SqsAsyncClient sqsAsyncClient;
    @SpyBean
    private StreamBridge streamBridge;

    @Test
    @SneakyThrows
    void givenInvalidUser_whenTryToSendRegisterMessage_shouldReturnErrorMessage() {
      // given
      val mockedStreamBridge = Mockito.mock(StreamBridge.class);
      userRegisterController = new UserRegisterController(mockedStreamBridge, userRegisterValidator);
      val nathan = User.builder().build();
      when(mockedStreamBridge.send(eq("userRegister"), any(Message.class))).thenReturn(false);

      // when
      val result = userRegisterController.register().apply(Mono.just(nathan)).block();

      // then
      assertThat(result)
              .extracting(ResponseEntity::getStatusCodeValue)
              .isEqualTo(400);
      assertThat(result.getBody().split("; "))
              .containsExactlyInAnyOrder(
                      "lastName must not be empty",
                      "firstName must not be empty",
                      "email must not be empty",
                      "password must not be empty");
    }

    @Test
    @SneakyThrows
    void givenValidUser_whenUnknownErrorOccurredDuringSendingRegisterMessage_shouldReturnErrorMessage() {
      // given
      val mockedStreamBridge = Mockito.mock(StreamBridge.class);
      userRegisterController = new UserRegisterController(mockedStreamBridge, userRegisterValidator);
      val nathan = User.builder()
              .firstName("Nathan")
              .lastName("Rich")
              .email("nathan.rich@smogian.com")
              .password("nGli7y{AW-MC")
              .build();
      when(mockedStreamBridge.send(eq("userRegister"), any(Message.class))).thenReturn(false);

      // when
      val result = userRegisterController.register().apply(Mono.just(nathan)).block();

      // then
      assertThat(result)
              .extracting(
                      ResponseEntity::getBody,
                      ResponseEntity::getStatusCodeValue)
              .containsExactly(
                      "failed to send message",
                      500
              );
    }

    @Test
    @SneakyThrows
    void givenValidUser_whenRegisterTheUser_shouldProceedTheHappyPath() {
      // given
      userRegisterController = new UserRegisterController(streamBridge, userRegisterValidator);
      val firstName = "Nathan".concat(String.valueOf(random.nextInt(100)));
      val nathan = User.builder()
              .firstName(firstName)
              .lastName("Rich")
              .email("nathan.rich@smogian.com")
              .password("nGli7y{AW-MC")
              .build();

      // when
      sqsAsyncClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(userRegisterQueue).build()).get();
      val result = userRegisterController.register().apply(Mono.just(nathan)).block();
      val message = objectMapper.readValue(
              sqsAsyncClient.receiveMessage(
                              ReceiveMessageRequest.builder()
                                      .queueUrl(userRegisterQueue)
                                      .maxNumberOfMessages(1)
                                      .build()).get()
                      .messages().stream().findFirst().get().body(),
              User.class);

      // then
      assertSoftly(softly -> {
        softly.assertThatCode(() -> verify(streamBridge, times(1)).send(eq("userRegister"), messageCaptor.capture()))
                .doesNotThrowAnyException();
        softly.assertThat(messageCaptor.getValue())
                .extracting(Message::getPayload)
                .isEqualTo(nathan);
        softly.assertThat(message).extracting(
                        User::getId,
                        User::getFirstName,
                        User::getLastName,
                        User::getEmail,
                        User::getPasswordHash)
                .containsExactly(
                        null,
                        firstName,
                        "Rich",
                        "nathan.rich@smogian.com",
                        "88b6fc3982b072a2ca2b07dce170d13ab402db91630a7c31433c7bb3fe4b0078"
                );
        softly.assertThat(result)
                .extracting(
                        ResponseEntity::getBody,
                        ResponseEntity::getStatusCodeValue)
                .containsExactly(
                        null,
                        200
                );
      });
    }
  }

  @Nested
  @ActiveProfiles("invalidQueue")
  class InvalidQueueProfile {
    @SpyBean
    private StreamBridge streamBridge;
    @Autowired
    private SqsAsyncClient sqsAsyncClient;

    @BeforeEach
    void setup() {
      userRegisterController = new UserRegisterController(streamBridge, userRegisterValidator);
    }

    @Test
    @SneakyThrows
    void givenQueueIsNotSpecified_whenRegisterTheUser_shouldReturnErrors() {
      // given
      val firstName = "Nathan".concat(String.valueOf(random.nextInt(100)));
      val nathan = User.builder()
              .firstName(firstName)
              .lastName("Rich")
              .email("nathan.rich@smogian.com")
              .password("nGli7y{AW-MC")
              .build();

      // when
      val result = userRegisterController.register().apply(Mono.just(nathan)).block();

      // then
      assertSoftly(softly -> {
        softly.assertThatCode(() -> verify(streamBridge, times(1)).send(eq("userRegister"), messageCaptor.capture()))
                .doesNotThrowAnyException();
        softly.assertThat(messageCaptor.getValue())
                .extracting(Message::getPayload)
                .isEqualTo(nathan);

        softly.assertThat(result)
                .extracting(ResponseEntity::getBody)
                .asString()
                .startsWith("The specified queue does not exist for this wsdl version.");
        softly.assertThat(result)
                .extracting(ResponseEntity::getStatusCodeValue)
                .isEqualTo(500);
      });
    }
  }
}