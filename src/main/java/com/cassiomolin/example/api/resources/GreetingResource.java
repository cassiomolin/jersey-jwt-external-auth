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
 * Greetings resource.
 *
 * @author cassiomolin
 */
@RequestScoped
@Path("/greetings")
public class GreetingResource {

    @Inject
    private UserService userService;

    @GET
    @Path("/public")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getPublicGreeting(@Context SecurityContext securityContext) {
        return Response.ok("Hello World!").build();
    }

    @GET
    @Secured
    @Path("/protected")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getProtectedGreeting(@Context SecurityContext securityContext) {
        Optional<User> userOptional = userService.findByEmail(securityContext.getUserPrincipal().getName());
        if (userOptional.isPresent()) {
            String greeting = "Hello " + userOptional.get().getFirstName() + "!";
            return Response.ok(greeting).build();
        } else {
            return Response.ok("Hello!").build();
        }
    }
}
