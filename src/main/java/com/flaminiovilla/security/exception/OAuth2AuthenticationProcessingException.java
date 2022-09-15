package com.flaminiovilla.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * The type OAuth2 authentication processing exception.
 */
public class OAuth2AuthenticationProcessingException extends AuthenticationException {
    /**
     * Instantiates a new OAuth2 authentication processing exception.
     * @param msg the message to be shown when the error is thrown
     * @param t the throwable error that caused the exception
     */
    public OAuth2AuthenticationProcessingException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Instantiates a new OAuth2 authentication processing exception.
     * @param msg the message to be shown when the error is thrown
     */
    public OAuth2AuthenticationProcessingException(String msg) {
        super(msg);
    }
}
