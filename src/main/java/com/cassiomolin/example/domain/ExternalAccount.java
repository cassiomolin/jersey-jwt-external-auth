package com.cassiomolin.example.domain;

/**
 * Domain class that represents an external account.
 *
 * @author cassiomolin
 */
public class ExternalAccount {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String accessToken;
    private AuthenticationProvider provider;

    public String getId() {
        return id;
    }

    public ExternalAccount setId(String id) {
        this.id = id;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ExternalAccount setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public ExternalAccount setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public ExternalAccount setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public ExternalAccount setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public AuthenticationProvider getProvider() {
        return provider;
    }

    public ExternalAccount setProvider(AuthenticationProvider provider) {
        this.provider = provider;
        return this;
    }
}
