package com.cassiomolin.example.service.token;

import com.cassiomolin.example.common.configuration.Configurable;
import com.cassiomolin.example.domain.AuthenticationTokenDetails;
import com.cassiomolin.example.exception.InvalidAuthenticationTokenException;
import io.jsonwebtoken.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Component which provides operations for parsing JWT tokens.
 *
 * @author cassiomolin
 */
@Dependent
class AuthenticationTokenParser {

    /**
     * Secret for signing and verifying the token signature.
     */
    @Inject
    @Configurable("authentication.jwt.secret")
    private String secret;

    /**
     * Allowed clock skew for verifying the token signature (in seconds).
     */
    @Inject
    @Configurable("authentication.jwt.clockSkew")
    private Long clockSkew;

    /**
     * Identifies the recipients that the JWT token is intended for.
     */
    @Inject
    @Configurable("authentication.jwt.audience")
    private String audience;

    /**
     * Parse a JWT token.
     *
     * @param authenticationToken
     * @return
     */
    AuthenticationTokenDetails parseAuthenticationToken(String authenticationToken) {

        try {

            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .requireAudience(audience)
                    .setAllowedClockSkewSeconds(clockSkew)
                    .parseClaimsJws(authenticationToken)
                    .getBody();

            return new AuthenticationTokenDetails.Builder()
                    .withId(extractTokenIdFromClaims(claims))
                    .withSubject(extractUsernameFromClaims(claims))
                    .withIssuedDate(extractIssuedDateFromClaims(claims))
                    .withExpirationDate(extractExpirationDateFromClaims(claims))
                    .build();

        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException e) {
            throw new InvalidAuthenticationTokenException("Invalid authentication token", e);
        } catch (ExpiredJwtException e) {
            throw new InvalidAuthenticationTokenException("Expired authentication token", e);
        } catch (InvalidClaimException e) {
            throw new InvalidAuthenticationTokenException("Invalid value for claim \"" + e.getClaimName() + "\" for authentication token", e);
        } catch (Exception e) {
            throw new InvalidAuthenticationTokenException("Invalid authentication token", e);
        }
    }

    /**
     * Extract the token identifier from the token claims.
     *
     * @param claims
     * @return Identifier of the JWT token
     */
    private String extractTokenIdFromClaims(@NotNull Claims claims) {
        return (String) claims.get(Claims.ID);
    }

    /**
     * Extract the username from the token claims.
     *
     * @param claims
     * @return Username from the JWT token
     */
    private String extractUsernameFromClaims(@NotNull Claims claims) {
        return claims.getSubject();
    }

    /**
     * Extract the issued date from the token claims.
     *
     * @param claims
     * @return Issued date of the JWT token
     */
    private ZonedDateTime extractIssuedDateFromClaims(@NotNull Claims claims) {
        return ZonedDateTime.ofInstant(claims.getIssuedAt().toInstant(), ZoneId.systemDefault());
    }

    /**
     * Extract the expiration date from the token claims.
     *
     * @param claims
     * @return Expiration date of the JWT token
     */
    private ZonedDateTime extractExpirationDateFromClaims(@NotNull Claims claims) {
        return ZonedDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
    }
}