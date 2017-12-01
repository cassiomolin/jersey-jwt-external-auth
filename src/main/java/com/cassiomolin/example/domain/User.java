package com.cassiomolin.example.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain class that represents an user.
 *
 * @author cassiomolin
 */
public class User {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private List<ExternalAccountLink> externalAccounts = new ArrayList<>();

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public User setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public List<ExternalAccountLink> getExternalAccounts() {
        return externalAccounts;
    }

    public User setExternalAccounts(List<ExternalAccountLink> externalAccounts) {
        this.externalAccounts = externalAccounts;
        return this;
    }
}
