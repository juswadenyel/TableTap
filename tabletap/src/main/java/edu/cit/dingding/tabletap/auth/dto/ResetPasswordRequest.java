package edu.cit.dingding.tabletap.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// This request redeems a reset token and supplies a replacement password.
public record ResetPasswordRequest(@NotBlank String token, @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String newPassword, @NotBlank String confirmPassword) { }
