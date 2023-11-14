package com.smogian.user.registration.controller;

import com.google.common.base.Throwables;
import com.smogian.user.registration.domain.User;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.function.Function;
import java.util.logging.Level;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class UserRegisterController {
  private final StreamBridge streamBridge;
  private final UserRegisterValidator userRegisterValidator;

  @Bean
  public Function<Mono<User>, Mono<ResponseEntity<String>>> register() {
    return userMono -> userMono.map(userRegisterValidator::validate)
            .log("Start to process", Level.INFO, SignalType.ON_NEXT)
            .map(user -> streamBridge.send("userRegister", MessageBuilder.withPayload(user).build()))
            .map(result -> Boolean.TRUE.equals(result)
                    ? ResponseEntity.ok().<String>build()
                    : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to send message"))
            .onErrorResume(ValidationException.class,
                    error -> Mono.just(ResponseEntity.badRequest().body(error.getMessage())))
            .onErrorResume(MessagingException.class,
                    error -> Mono.just(ResponseEntity.
                            status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Throwables.getRootCause(error).getMessage())))
            .log("Complete process", Level.INFO, SignalType.ON_COMPLETE);
  }
}
