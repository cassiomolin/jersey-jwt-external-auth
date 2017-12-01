package com.cassiomolin.example.api.resources;

import com.cassiomolin.example.api.providers.Secured;
import com.cassiomolin.example.api.model.AuthenticationToken;
import com.cassiomolin.example.domain.AuthenticationProvider;
import com.cassiomolin.example.domain.ExternalAccount;
import com.cassiomolin.example.service.token.AuthenticationTokenService;
import com.cassiomolin.example.service.external.GoogleAuthenticationService;
import com.cassiomolin.example.service.UserService;
import org.hibernate.validator.constraints.NotBlank;

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
 * Google authentication resource.
 *
 * @author cassiomolin
 */
@RequestScoped
@Path("/auth/google")
public class GoogleAuthenticationResource {

    @Inject
    private UserService userService;

    @Inject
    private AuthenticationTokenService authenticationTokenService;

    @Inject
    private GoogleAuthenticationService googleAuthenticationService;

    @POST
    public Response linkAccount() throws URISyntaxException {
        String authorizationUrl = googleAuthenticationService.getAuthorizationUrl();
        return Response.temporaryRedirect(new URI(authorizationUrl)).build();
    }

    @GET
    @Path("/callback")
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleCallback(@NotNull @NotBlank @QueryParam("code") String code) throws InterruptedException, ExecutionException, IOException {

        String accessToken = googleAuthenticationService.getAccessToken(code);
        ExternalAccount externalAccount = googleAuthenticationService.getExternalAccountDetails(accessToken);
        userService.linkAccount(externalAccount);

        String token = authenticationTokenService.issueAuthenticationToken(externalAccount.getEmail());
        return Response.ok(new AuthenticationToken(token)).build();
    }

    @DELETE
    @Secured
    public Response unlinkAccount(@Context SecurityContext securityContext) throws URISyntaxException {
        userService.unlinkAccount(securityContext.getUserPrincipal().getName(), AuthenticationProvider.GOOGLE);
        return Response.noContent().build();
    }
}
