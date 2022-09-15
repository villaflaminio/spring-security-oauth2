package com.flaminiovilla.security.repository;

import com.flaminiovilla.security.model.RefreshToken;
import com.flaminiovilla.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;

/**
 * Repository to manage all the operations related to the refresh token.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);

  @Transactional
  void deleteAllByExpiryDateIsLessThan(Instant expiryDate);

  @Modifying
  @Transactional
  void deleteByExpiryDateIsLessThan(Instant expiryDate);

  @Modifying
  int deleteByUser(User user);
}
