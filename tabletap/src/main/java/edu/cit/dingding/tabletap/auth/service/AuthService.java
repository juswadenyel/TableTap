package edu.cit.dingding.tabletap.auth.service;

import edu.cit.dingding.tabletap.auth.dto.AuthResponse;
import edu.cit.dingding.tabletap.auth.dto.LoginRequest;
import edu.cit.dingding.tabletap.auth.dto.RegisterRequest;
import edu.cit.dingding.tabletap.user.dto.UserResponse;
import edu.cit.dingding.tabletap.user.model.User;
import edu.cit.dingding.tabletap.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

// This service registers users and authenticates login requests.
@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final UserService userService;
    private final TokenService tokenService;

    public AuthService(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        User user = userService.registerDiner(
                request.fullName(),
                request.email(),
                request.phone(),
                request.password()
        );
        return createAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userService.findByEmail(request.email())
                .filter(account -> userService.passwordMatches(account, request.password()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS));
        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
        return new AuthResponse(tokenService.createToken(user), UserResponse.from(user));
    }
}
