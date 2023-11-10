package com.smogian.user.registration.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.hash.Hashing;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

//TODO Move this class to shared model lib

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
  private static final String CREDENTIAL_PATTERN_ERROR_MESSAGE =
          "must contain at least one digit, one lowercase letter, one uppercase letter, " +
                  "one special character, and between 8 characters and 20 characters";
  String id;
  @NotEmpty
  @Email
  String email;
  @NotEmpty
  @Size(min = 2)
  String firstName;
  @NotEmpty
  @Size(min = 2)
  String lastName;
  @NotEmpty
  @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–{}:;',?/*~$^+=<>]).{8,20}$",
          message = CREDENTIAL_PATTERN_ERROR_MESSAGE)
  @Getter(AccessLevel.NONE)
  @ToString.Exclude
  String password;
  String passwordHash;

  public static class UserBuilder {
    public UserBuilder password(final String password) {
      this.password = password;
      this.passwordHash = Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
      return this;
    }
  }
}
