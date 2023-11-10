package com.smogian.user.registration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

public class AwsTestConfiguration {

  @Profile({"test", "invalidQueue"})
  @Bean
  public SqsAsyncClient amazonSQS() {
    return SqsAsyncClient.builder()
            .endpointOverride(URI.create("http://localhost:4566/"))
            .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("ANUJDEKAVADIYAEXAMPLE", "2QvM4/Tdmf38SkcD/qalvXO4EXAMPLEKEY")))
            .region(Region.CA_CENTRAL_1)
            .build();
  }
}
