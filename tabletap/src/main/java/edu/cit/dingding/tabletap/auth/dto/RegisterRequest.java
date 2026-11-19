package edu.cit.dingding.tabletap.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

// This request carries the details needed to register a diner account.
public record RegisterRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
        @Pattern(regexp = "^$|^[0-9+ ]{7,15}$", message = "Phone must contain 7 to 15 digits, plus signs, or spaces") String phone,
        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password
) {
}
