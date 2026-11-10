package edu.cit.dingding.tabletap.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// This request carries the credentials needed to log in.
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
