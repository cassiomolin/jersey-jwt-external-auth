package com.cassiomolin.example.api.model;

/**
 * API model that represents an authentication token.
 *
 * @author cassiomolin
 */
public class AuthenticationToken {

    private String token;

    public AuthenticationToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}