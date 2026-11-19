package edu.cit.dingding.tabletap.auth.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlocklist {
    private final ConcurrentHashMap<String, Instant> revoked = new ConcurrentHashMap<>();

    public void revoke(String jti, Instant expiresAt) {
        if (jti != null && expiresAt != null) revoked.put(jti, expiresAt);
    }

    public boolean isRevoked(String jti) {
        Instant expiresAt = revoked.get(jti);
        if (expiresAt == null) return false;
        if (!expiresAt.isAfter(Instant.now())) {
            revoked.remove(jti, expiresAt);
            return false;
        }
        return true;
    }

    @Scheduled(fixedDelay = 60000)
    public void cleanupExpired() {
        Instant now = Instant.now();
        revoked.entrySet().removeIf(entry -> !entry.getValue().isAfter(now));
    }
}
