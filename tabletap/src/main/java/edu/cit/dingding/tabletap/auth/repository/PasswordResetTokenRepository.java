package edu.cit.dingding.tabletap.auth.repository;

import edu.cit.dingding.tabletap.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// This repository reads and updates password reset tokens.
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from PasswordResetToken token where token.tokenHash = :hash")
    Optional<PasswordResetToken> findLockedByTokenHash(@Param("hash") String hash);
    List<PasswordResetToken> findByUserUserIdAndUsedAtIsNull(UUID userId);
}
