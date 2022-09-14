package com.flaminiovilla.security.security;

import com.flaminiovilla.security.config.AppProperties;
import com.flaminiovilla.security.exception.TokenRefreshException;
import com.flaminiovilla.security.model.RefreshToken;
import com.flaminiovilla.security.model.User;
import com.flaminiovilla.security.repository.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RefreshTokenService {

  private RefreshTokenRepository refreshTokenRepository;
  private Environment env;




  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }

  public RefreshToken createRefreshToken(User user) {
    RefreshToken refreshToken = new RefreshToken();

    refreshToken.setUser(user);
    refreshToken.setExpiryDate(Instant.now().plusMillis(Long.parseLong(Objects.requireNonNull(env.getProperty("app.auth.refreshTokenExpiration")))));
    refreshToken.setToken(UUID.randomUUID().toString());
    refreshToken = refreshTokenRepository.save(refreshToken);

    return refreshToken;
  }

  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
      refreshTokenRepository.delete(token);
      throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
    }

    return token;
  }


}
