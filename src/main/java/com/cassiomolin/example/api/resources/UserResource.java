package com.cassiomolin.example.api.resources;

import com.cassiomolin.example.api.providers.Secured;
import com.cassiomolin.example.domain.User;
import com.cassiomolin.example.service.UserService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Optional;

/**
 * User resource.
 *
 * @author cassiomolin
 */
@RequestScoped
@Path("/users")
public class UserResource {

    @Inject
    private UserService userService;

    @GET
    @Secured
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrentUser(@Context SecurityContext securityContext) {
        Optional<User> userOptional = userService.findByEmail(securityContext.getUserPrincipal().getName());
        return Response.ok(userOptional.orElse(null)).build();
    }
}
