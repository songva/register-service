package com.smogian.user.registration.controller;

import com.google.common.base.Throwables;
import com.smogian.user.registration.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class UserRegisterController {
  private final StreamBridge streamBridge;
  private final UserRegisterValidator userRegisterValidator;

  @Bean
  public Function<Mono<User>, Mono<List<String>>> register() {
    return userMono -> userMono.map(userRegisterValidator::validate)
            .map(user -> streamBridge.send("createUser", MessageBuilder.withPayload(user).build()))
            .map(result -> Boolean.TRUE.equals(result)
                    ? Collections.emptyList()
                    : Collections.singletonList("failed to send message"))
            .onErrorResume(error -> {
              log.error("Register Error: {}", error.getMessage(), error);
              return Mono.just(Collections.singletonList(Throwables.getRootCause(error).getMessage()));
            })
            .flatMap(error -> Mono.just(error.stream().map(Object::toString).toList()));
  }
}
