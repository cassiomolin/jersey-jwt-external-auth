package com.cassiomolin.example.service.external;

import com.cassiomolin.example.domain.ExternalAccount;
import com.cassiomolin.example.exception.OAuthRequestException;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.common.base.Strings;

/**
 * Base class for authentication services that use OAuth 2.0.
 *
 * @author cassiomolin
 */
public abstract class BaseOAuth20AuthenticationService {

    protected OAuth20Service service;

    public String getAuthorizationUrl() {
        return service.getAuthorizationUrl();
    }

    public String getAccessToken(String code) {

        if (Strings.isNullOrEmpty(code)) {
            throw new IllegalArgumentException("Code must not be null or empty");
        }

        try {
            OAuth2AccessToken accessToken = service.getAccessToken(code);
            return accessToken.getAccessToken();
        } catch (OAuth2AccessTokenErrorResponse e) {
            throw new OAuthRequestException("Access token request has failed: " + e.getErrorDescription(), e);
        } catch (Exception e) {
            throw new OAuthRequestException("Access token request has failed", e);
        }
    }

    public ExternalAccount getExternalAccountDetails(String accessToken) {

        if (Strings.isNullOrEmpty(accessToken)) {
            throw new IllegalArgumentException("Access token must not be null or empty");
        }

        Response response = executeRequest(accessToken, new OAuthRequest(Verb.GET, getProfileUrl()));
        if (response.isSuccessful()) {
            ExternalAccount externalAccount = parseResponse(response);
            externalAccount.setAccessToken(accessToken);
            return externalAccount;
        } else {
            throw new OAuthRequestException("Request has failed: " + response.getMessage());
        }
    }

    private Response executeRequest(String accessToken, OAuthRequest request) {

        try {
            service.signRequest(accessToken, request);
            return service.execute(request);
        } catch (Exception e) {
            throw new OAuthRequestException("Can't execute request", e);
        }
    }

    abstract String getProfileUrl();

    abstract ExternalAccount parseResponse(Response response);
}
