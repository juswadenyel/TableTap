package edu.cit.dingding.tabletap.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// This request contains the current and replacement password values.
public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String newPassword,
        @NotBlank String confirmPassword
) { }
