package com.cassiomolin.example.exception;

/**
 * Thrown if an authentication token is invalid.
 *
 * @author cassiomolin
 */
public class InvalidAuthenticationTokenException extends RuntimeException {

    public InvalidAuthenticationTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}