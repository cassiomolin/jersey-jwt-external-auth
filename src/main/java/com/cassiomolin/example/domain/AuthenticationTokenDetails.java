package com.cassiomolin.example.domain;

import java.time.ZonedDateTime;

/**
 * Domain class that holds details about an authentication token.
 *
 * @author cassiomolin
 */
public final class AuthenticationTokenDetails {

    private final String id;
    private final String subject;
    private final ZonedDateTime issuedDate;
    private final ZonedDateTime expirationDate;

    private AuthenticationTokenDetails(String id, String subject, ZonedDateTime issuedDate, ZonedDateTime expirationDate) {
        this.id = id;
        this.subject = subject;
        this.issuedDate = issuedDate;
        this.expirationDate = expirationDate;
    }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public ZonedDateTime getIssuedDate() {
        return issuedDate;
    }

    public ZonedDateTime getExpirationDate() {
        return expirationDate;
    }

    /**
     * Builder for {@link AuthenticationTokenDetails}.
     */
    public static class Builder {

        private String id;
        private String subject;
        private ZonedDateTime issuedDate;
        private ZonedDateTime expirationDate;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withIssuedDate(ZonedDateTime issuedDate) {
            this.issuedDate = issuedDate;
            return this;
        }

        public Builder withExpirationDate(ZonedDateTime expirationDate) {
            this.expirationDate = expirationDate;
            return this;
        }

        public AuthenticationTokenDetails build() {
            return new AuthenticationTokenDetails(id, subject, issuedDate, expirationDate);
        }
    }
}