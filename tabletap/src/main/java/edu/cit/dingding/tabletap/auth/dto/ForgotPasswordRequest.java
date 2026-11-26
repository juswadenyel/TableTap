package edu.cit.dingding.tabletap.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// This request identifies the account that needs a password reset link.
public record ForgotPasswordRequest(@NotBlank @Email String email) { }
