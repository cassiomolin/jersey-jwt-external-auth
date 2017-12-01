package com.cassiomolin.example.service;

import com.cassiomolin.example.domain.AuthenticationProvider;
import com.cassiomolin.example.domain.ExternalAccount;
import com.cassiomolin.example.domain.ExternalAccountLink;
import com.cassiomolin.example.domain.User;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User service.
 *
 * @author cassiomolin
 */
@ApplicationScoped
public class UserService {

    private static List<User> users = new ArrayList<>();

    /**
     * Link an external account with an application user using the email.
     * <p>
     * If no user is found in the application with the given email, a new user will be created using the external account details.
     *
     * @param externalAccount
     */
    public void linkAccount(ExternalAccount externalAccount) {

        User user;

        Optional<User> optionalUser = findByEmail(externalAccount.getEmail());
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setEmail(externalAccount.getEmail());
            user.setFirstName(externalAccount.getFirstName());
            user.setLastName(externalAccount.getLastName());
        }

        // Remove existing account
        user.getExternalAccounts().removeIf(externalAccountLink -> externalAccountLink.getProvider().equals(externalAccount.getProvider()));

        ExternalAccountLink externalAccountLink = new ExternalAccountLink();
        externalAccountLink.setId(externalAccount.getId());
        externalAccountLink.setAccessToken(externalAccount.getAccessToken());
        externalAccountLink.setProvider(externalAccount.getProvider());
        user.getExternalAccounts().add(externalAccountLink);

        users.add(user);
    }

    /**
     * Unlink an account from the user with the given email. No user will be deleted, just the link with the external account.
     *
     * @param email
     * @param authenticationProvider
     */
    public void unlinkAccount(String email, AuthenticationProvider authenticationProvider) {
        findByEmail(email).ifPresent(user -> user.getExternalAccounts().removeIf(externalAccountLink -> externalAccountLink.getProvider().equals(authenticationProvider)));
    }

    /**
     * Find a user by email.
     *
     * @param email
     * @return
     */
    public Optional<User> findByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }
}
