package com.cassiomolin.example.service.token;

import com.cassiomolin.example.common.configuration.Configurable;
import com.cassiomolin.example.domain.AuthenticationTokenDetails;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Service for issuing and parsing authentication tokens.
 *
 * @author cassiomolin
 */
@ApplicationScoped
public class AuthenticationTokenService {

    /**
     * How long the token is valid for (in seconds).
     */
    @Inject
    @Configurable("authentication.jwt.validFor")
    private Long validFor;

    @Inject
    private AuthenticationTokenIssuer tokenIssuer;

    @Inject
    private AuthenticationTokenParser tokenParser;

    /**
     * Issue a token for a user with the given authorities.
     *
     * @param subject
     * @return
     */
    public String issueAuthenticationToken(String subject) {

        String id = generateTokenIdentifier();
        ZonedDateTime issuedDate = ZonedDateTime.now();
        ZonedDateTime expirationDate = calculateExpirationDate(issuedDate);

        AuthenticationTokenDetails authenticationTokenDetails = new AuthenticationTokenDetails.Builder()
                .withId(id)
                .withSubject(subject)
                .withIssuedDate(issuedDate)
                .withExpirationDate(expirationDate)
                .build();

        return tokenIssuer.issueAuthenticationToken(authenticationTokenDetails);
    }

    /**
     * Parse and validate the token.
     *
     * @param token
     * @return
     */
    public AuthenticationTokenDetails parseAuthenticationToken(String token) {
        return tokenParser.parseAuthenticationToken(token);
    }

    /**
     * Calculate the expiration date for a token.
     *
     * @param issuedDate
     * @return
     */
    private ZonedDateTime calculateExpirationDate(ZonedDateTime issuedDate) {
        return issuedDate.plusSeconds(validFor);
    }

    /**
     * Generate a token identifier.
     *
     * @return
     */
    private String generateTokenIdentifier() {
        return UUID.randomUUID().toString();
    }
}