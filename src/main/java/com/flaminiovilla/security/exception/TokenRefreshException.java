package com.flaminiovilla.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * TokenRefreshException is thrown when the token cannot be refreshed.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Token refresh exception.
   * @param token the token that cannot be refreshed
   * @param message the message to be shown when the error is thrown
   */
  public TokenRefreshException(String token, String message) {
    super(String.format("Failed for [%s]: %s", token, message));
  }
}
