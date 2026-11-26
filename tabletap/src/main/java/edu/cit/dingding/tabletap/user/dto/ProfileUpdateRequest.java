package edu.cit.dingding.tabletap.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// This request contains the editable fields for the authenticated user's profile.
public record ProfileUpdateRequest(
        @NotBlank(message = "Full name is required") @Size(max = 100, message = "Full name must be at most 100 characters") String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
        @Pattern(regexp = "^$|^[0-9+ ]{7,15}$", message = "Phone must contain 7 to 15 digits, plus signs, or spaces") String phone
) { }
