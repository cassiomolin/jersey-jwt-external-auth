package com.cassiomolin.example.exception;

/**
 * Thrown if an OAuth request has failed.
 *
 * @author cassiomolin
 */
public class OAuthRequestException extends RuntimeException {

    public OAuthRequestException(String message) {
        super(message);
    }

    public OAuthRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}