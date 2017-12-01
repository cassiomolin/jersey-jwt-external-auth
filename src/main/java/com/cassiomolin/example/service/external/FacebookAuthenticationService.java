package com.cassiomolin.example.service.external;

import com.cassiomolin.example.common.configuration.Configurable;
import com.cassiomolin.example.domain.AuthenticationProvider;
import com.cassiomolin.example.domain.ExternalAccount;
import com.cassiomolin.example.exception.OAuthRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.Response;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Facebook authentication service.
 *
 * @author cassiomolin
 */
@ApplicationScoped
public class FacebookAuthenticationService extends BaseOAuth20AuthenticationService {

    @Inject
    @Configurable("authentication.provider.facebook.clientId")
    private String clientId;

    @Inject
    @Configurable("authentication.provider.facebook.clientSecret")
    private String clientSecret;

    @Inject
    @Configurable("authentication.provider.facebook.scope")
    private String scope;

    @Inject
    @Configurable("authentication.provider.facebook.callbackUrl")
    private String callbackUrl;

    @Inject
    @Configurable("authentication.provider.facebook.profileUrl")
    private String profileUrl;

    @Inject
    private ObjectMapper mapper;

    @PostConstruct
    void init() {
        this.service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .scope(scope)
                .callback(callbackUrl)
                .build(FacebookApi.instance());
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
            externalAccount.setEmail(tree.get("email").asText());
            externalAccount.setFirstName(tree.get("first_name").asText());
            externalAccount.setLastName(tree.get("last_name").asText());
            externalAccount.setProvider(AuthenticationProvider.FACEBOOK);

            return externalAccount;

        } catch (Exception e) {
            throw new OAuthRequestException("Can't parse response", e);
        }
    }
}
