package com.cassiomolin.example.api.providers;

import com.cassiomolin.example.common.configuration.Configurable;
import com.cassiomolin.example.domain.AuthenticationTokenDetails;
import com.cassiomolin.example.service.token.AuthenticationTokenService;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

/**
 * Authentication filter that validates the authentication token sent in the {@code Authorization} header of the request.
 *
 * @author cassiomolin
 */
@Secured
@Provider
@ApplicationScoped
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    /**
     * Application realm name.
     */
    @Inject
    @Configurable("authentication.realm")
    private String realm;

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    @Inject
    private AuthenticationTokenService authenticationTokenService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            abortWithUnauthorized(requestContext);
            return;
        }

        String authenticationToken = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();
        validateAuthenticationToken(authenticationToken, requestContext);
    }

    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null &&
                authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, AUTHENTICATION_SCHEME + " realm=\"" + realm + "\"")
                        .build());
    }

    private void validateAuthenticationToken(String authenticationToken, ContainerRequestContext requestContext) {

        try {

            AuthenticationTokenDetails authenticationTokenDetails = authenticationTokenService.parseAuthenticationToken(authenticationToken);
            final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
            requestContext.setSecurityContext(new SecurityContext() {

                @Override
                public Principal getUserPrincipal() {
                    return authenticationTokenDetails::getSubject;
                }

                @Override
                public boolean isUserInRole(String role) {
                    return true;
                }

                @Override
                public boolean isSecure() {
                    return currentSecurityContext.isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return AUTHENTICATION_SCHEME;
                }
            });

        } catch (Exception e) {
            abortWithUnauthorized(requestContext);
        }
    }
}