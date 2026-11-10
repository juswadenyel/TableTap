package edu.cit.dingding.tabletap.user.dto;

import edu.cit.dingding.tabletap.user.model.Role;
import edu.cit.dingding.tabletap.user.model.User;

import java.util.UUID;

// This response exposes public account details without the password hash.
public record UserResponse(UUID userId, String fullName, String email, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getUserId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
