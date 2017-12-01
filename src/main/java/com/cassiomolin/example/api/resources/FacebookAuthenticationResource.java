package com.cassiomolin.example.api.resources;

import com.cassiomolin.example.api.providers.Secured;
import com.cassiomolin.example.api.model.AuthenticationToken;
import com.cassiomolin.example.domain.AuthenticationProvider;
import com.cassiomolin.example.domain.ExternalAccount;
import com.cassiomolin.example.service.token.AuthenticationTokenService;
import com.cassiomolin.example.service.external.FacebookAuthenticationService;
import com.cassiomolin.example.service.UserService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

/**
 * Facebook authentication resource.
 *
 * @author cassiomolin
 */
@RequestScoped
@Path("/auth/facebook")
public class FacebookAuthenticationResource {

    @Inject
    private UserService userService;

    @Inject
    private AuthenticationTokenService authenticationTokenService;

    @Inject
    private FacebookAuthenticationService facebookAuthenticationService;

    @POST
    public Response linkAccount() throws URISyntaxException {
        String authorizationUrl = facebookAuthenticationService.getAuthorizationUrl();
        return Response.temporaryRedirect(new URI(authorizationUrl)).build();
    }

    @GET
    @Path("/callback")
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleCallback(@NotNull @QueryParam("code") String code) throws InterruptedException, ExecutionException, IOException {

        String accessToken = facebookAuthenticationService.getAccessToken(code);
        ExternalAccount externalAccount = facebookAuthenticationService.getExternalAccountDetails(accessToken);
        userService.linkAccount(externalAccount);

        String token = authenticationTokenService.issueAuthenticationToken(externalAccount.getEmail());
        return Response.ok(new AuthenticationToken(token)).build();
    }

    @DELETE
    @Secured
    public Response unlinkAccount(@Context SecurityContext securityContext) throws URISyntaxException {
        userService.unlinkAccount(securityContext.getUserPrincipal().getName(), AuthenticationProvider.FACEBOOK);
        return Response.noContent().build();
    }
}
