package com.cassiomolin.example.service.token;

import com.cassiomolin.example.common.configuration.Configurable;
import com.cassiomolin.example.domain.AuthenticationTokenDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwsHeader;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Date;

/**
 * Component which provides operations for issuing JWT tokens.
 *
 * @author cassiomolin
 */
@Dependent
class AuthenticationTokenIssuer {

    /**
     * Secret for signing and verifying the token signature.
     */
    @Inject
    @Configurable("authentication.jwt.secret")
    private String secret;

    /**
     * Identifies the recipients that the JWT token is intended for.
     */
    @Inject
    @Configurable("authentication.jwt.audience")
    private String audience;

    /**
     * Identifies the JWT token issuer.
     */
    @Inject
    @Configurable("authentication.jwt.issuer")
    private String issuer;

    /**
     * Issue a JWT token
     *
     * @param authenticationTokenDetails
     * @return
     */
    String issueAuthenticationToken(AuthenticationTokenDetails authenticationTokenDetails) {

        return Jwts.builder()
                .setId(authenticationTokenDetails.getId())
                .setIssuer(issuer)
                .setAudience(audience)
                .setSubject(authenticationTokenDetails.getSubject())
                .setIssuedAt(Date.from(authenticationTokenDetails.getIssuedDate().toInstant()))
                .setExpiration(Date.from(authenticationTokenDetails.getExpirationDate().toInstant()))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
}