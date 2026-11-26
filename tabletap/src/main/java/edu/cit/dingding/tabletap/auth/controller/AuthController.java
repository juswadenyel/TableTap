package edu.cit.dingding.tabletap.auth.controller;

import edu.cit.dingding.tabletap.auth.dto.AuthResponse;
import edu.cit.dingding.tabletap.auth.dto.LoginRequest;
import edu.cit.dingding.tabletap.auth.dto.RegisterRequest;
import edu.cit.dingding.tabletap.auth.dto.ForgotPasswordRequest;
import edu.cit.dingding.tabletap.auth.dto.ResetPasswordRequest;
import edu.cit.dingding.tabletap.auth.dto.GenericMessageResponse;
import edu.cit.dingding.tabletap.auth.service.AuthService;
import edu.cit.dingding.tabletap.auth.service.PasswordResetService;
import edu.cit.dingding.tabletap.auth.service.TokenBlocklist;
import edu.cit.dingding.tabletap.user.dto.UserResponse;
import edu.cit.dingding.tabletap.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

// This controller provides registration, login, logout, and current-user endpoints.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final TokenBlocklist tokenBlocklist;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, UserService userService, TokenBlocklist tokenBlocklist, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.userService = userService;
        this.tokenBlocklist = tokenBlocklist;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public GenericMessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpServletRequest servletRequest) {
        passwordResetService.requestReset(request.email(), servletRequest.getRemoteAddr());
        return new GenericMessageResponse(PasswordResetService.GENERIC_MESSAGE);
    }

    @GetMapping("/reset-password/validate")
    public void validateResetToken(@RequestParam(required = false) String token) {
        if (!passwordResetService.isValid(token)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset token is invalid, expired, or already used");
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.reset(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Jwt jwt) {
        tokenBlocklist.revoke(jwt.getId(), jwt.getExpiresAt());
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return userService.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
