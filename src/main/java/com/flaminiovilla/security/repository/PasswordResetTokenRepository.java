package com.flaminiovilla.security.repository;

import com.flaminiovilla.security.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    @Transactional
    void deleteAllByExpiryDateIsLessThan(Instant expiryDate);

    @Modifying
    @Transactional
    void deleteByExpiryDateIsLessThan(Instant expiryDate);
}