package edu.cit.dingding.tabletap.auth.dto;

import edu.cit.dingding.tabletap.user.dto.UserResponse;

// This response returns a signed token and the authenticated user's public details.
public record AuthResponse(String token, UserResponse user) {
}
