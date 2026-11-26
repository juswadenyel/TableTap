package edu.cit.dingding.tabletap.user.controller;

import edu.cit.dingding.tabletap.auth.service.TokenBlocklist;
import edu.cit.dingding.tabletap.user.dto.ChangePasswordRequest;
import edu.cit.dingding.tabletap.user.dto.ProfileUpdateRequest;
import edu.cit.dingding.tabletap.user.dto.UserResponse;
import edu.cit.dingding.tabletap.user.model.User;
import edu.cit.dingding.tabletap.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

// This controller manages the authenticated user's profile and password.
@RestController
@RequestMapping("/api/users/me")
public class UserController {
    private final UserService userService;
    private final TokenBlocklist tokenBlocklist;

    public UserController(UserService userService, TokenBlocklist tokenBlocklist) {
        this.userService = userService;
        this.tokenBlocklist = tokenBlocklist;
    }

    @GetMapping
    public UserResponse profile(@AuthenticationPrincipal Jwt jwt) {
        return requireUser(jwt).map(UserResponse::from).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @PutMapping
    public UserResponse updateProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ProfileUpdateRequest request) {
        return UserResponse.from(userService.updateProfile(userId(jwt), request.fullName(), request.email(), request.phone()));
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest request) {
        User user = requireUser(jwt).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!userService.passwordMatches(user, request.currentPassword())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        if (!request.newPassword().equals(request.confirmPassword())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New passwords do not match");
        if (userService.passwordMatches(user, request.newPassword())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must differ from current password");
        userService.updatePassword(user, request.newPassword());
        tokenBlocklist.revoke(jwt.getId(), jwt.getExpiresAt());
    }

    private java.util.Optional<User> requireUser(Jwt jwt) { return userService.findById(userId(jwt)); }
    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
