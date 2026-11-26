package edu.cit.dingding.tabletap.auth.service;

import edu.cit.dingding.tabletap.auth.dto.ResetPasswordRequest;
import edu.cit.dingding.tabletap.auth.model.PasswordResetToken;
import edu.cit.dingding.tabletap.auth.repository.PasswordResetTokenRepository;
import edu.cit.dingding.tabletap.user.model.User;
import edu.cit.dingding.tabletap.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;

// This service creates, validates, and redeems one-time password reset tokens.
@Service
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    public static final String GENERIC_MESSAGE = "If an account exists, a reset link has been sent";
    private final UserService userService;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final int expirationMinutes;
    private final String frontendUrl;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, LocalDateTime> requests = new ConcurrentHashMap<>();

    public PasswordResetService(UserService userService, PasswordResetTokenRepository tokenRepository, EmailService emailService,
            @Value("${app.reset-token.expiration-minutes:15}") int expirationMinutes,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.expirationMinutes = expirationMinutes;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public void requestReset(String email, String remoteAddress) {
        String normalized = email.trim().toLowerCase(java.util.Locale.ROOT);
        String rateKey = normalized + "|" + remoteAddress;
        LocalDateTime now = LocalDateTime.now();
        requests.entrySet().removeIf(entry -> entry.getValue().plusMinutes(1).isBefore(now));
        LocalDateTime previous = requests.putIfAbsent(rateKey, now);
        if (previous != null && previous.plusMinutes(1).isAfter(now)) return;
        requests.put(rateKey, now);
        userService.findByEmail(normalized).ifPresent(user -> {
            tokenRepository.findByUserUserIdAndUsedAtIsNull(user.getUserId()).forEach(token -> token.setUsedAt(now));
            tokenRepository.flush();
            byte[] bytes = new byte[32];
            secureRandom.nextBytes(bytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setTokenHash(hash(rawToken));
            token.setCreatedAt(now);
            token.setExpiresAt(now.plusMinutes(expirationMinutes));
            tokenRepository.save(token);
            String link = frontendUrl.replaceAll("/$", "") + "/reset-password?token=" + rawToken;
            try { emailService.sendPasswordReset(user.getEmail(), link, expirationMinutes); }
            catch (RuntimeException exception) {
                logger.warn("Password reset email delivery failed ({})", exception.getClass().getSimpleName());
            }
        });
    }

    @Transactional(readOnly = true)
    public boolean isValid(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return false;
        return tokenRepository.findByTokenHash(hash(rawToken)).filter(this::active).isPresent();
    }

    @Transactional
    public void reset(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New passwords do not match");
        PasswordResetToken token = tokenRepository.findLockedByTokenHash(hash(request.token()))
                .filter(this::active).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset token is invalid, expired, or already used"));
        User user = token.getUser();
        userService.updatePassword(user, request.newPassword());
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.findByUserUserIdAndUsedAtIsNull(user.getUserId()).forEach(other -> other.setUsedAt(now));
        token.setUsedAt(now);
        tokenRepository.save(token);
    }

    private boolean active(PasswordResetToken token) { return token.getUsedAt() == null && token.getExpiresAt().isAfter(LocalDateTime.now()); }

    private String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }
}
