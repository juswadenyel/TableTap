package edu.cit.dingding.tabletap.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// This request carries the details needed to register a diner account.
public record RegisterRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        String phone,
        @NotBlank @Size(min = 8) String password
) {
}
