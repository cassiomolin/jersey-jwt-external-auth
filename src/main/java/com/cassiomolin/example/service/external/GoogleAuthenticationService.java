package com.cassiomolin.example.service.external;

import com.cassiomolin.example.common.configuration.Configurable;
import com.cassiomolin.example.domain.AuthenticationProvider;
import com.cassiomolin.example.domain.ExternalAccount;
import com.cassiomolin.example.exception.OAuthRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Google authentication service.
 *
 * @author cassiomolin
 */
@ApplicationScoped
public class GoogleAuthenticationService extends BaseOAuth20AuthenticationService {

    @Inject
    @Configurable("authentication.provider.google.clientId")
    private String clientId;

    @Inject
    @Configurable("authentication.provider.google.clientSecret")
    private String clientSecret;

    @Inject
    @Configurable("authentication.provider.google.scope")
    private String scope;

    @Inject
    @Configurable("authentication.provider.google.callbackUrl")
    private String callbackUrl;

    @Inject
    @Configurable("authentication.provider.google.profileUrl")
    private String profileUrl;

    @Inject
    private ObjectMapper mapper;

    @PostConstruct
    void init() {
        this.service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .scope(scope)
                .callback(callbackUrl)
                .build(GoogleApi20.instance());
    }

    @Override
    String getProfileUrl() {
        return profileUrl;
    }

    @Override
    ExternalAccount parseResponse(Response response) {

        try {

            JsonNode tree = mapper.readTree(response.getBody());

            ExternalAccount externalAccount = new ExternalAccount();
            externalAccount.setId(tree.get("id").asText());

            String email = null;
            for (JsonNode node : tree.get("emails")) {
                if ("account".equals(node.get("type").asText())) {
                    email = node.get("value").asText();
                }
            }

            externalAccount.setEmail(email);
            externalAccount.setFirstName(tree.get("name").get("givenName").asText());
            externalAccount.setLastName(tree.get("name").get("familyName").asText());
            externalAccount.setProvider(AuthenticationProvider.GOOGLE);

            return externalAccount;

        } catch (Exception e) {
            throw new OAuthRequestException("Can't parse response", e);
        }
    }
}
