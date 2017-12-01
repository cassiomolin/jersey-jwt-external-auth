package com.cassiomolin.example.domain;

/**
 * Domain class with some basic details of an external account.
 *
 * @author cassiomolin
 */
public class ExternalAccountLink {

    private String id;
    private String accessToken;
    private AuthenticationProvider provider;

    public String getId() {
        return id;
    }

    public ExternalAccountLink setId(String id) {
        this.id = id;
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public ExternalAccountLink setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public AuthenticationProvider getProvider() {
        return provider;
    }

    public ExternalAccountLink setProvider(AuthenticationProvider provider) {
        this.provider = provider;
        return this;
    }
}
