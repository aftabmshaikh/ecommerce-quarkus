package com.ecommerce.user.controller;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User API", description = "APIs for managing users")
public class UserController {

    @Inject
    UserService userService;

    @GET
    @RolesAllowed("ADMIN")
    @Operation(summary = "Get all users")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Get user by ID")
    public UserResponse getUserById(@PathParam("id") UUID id) {
        return userService.getUserById(id);
    }

    @GET
    @Path("/username/{username}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Get user by username")
    public UserResponse getUserByUsername(@PathParam("username") String username) {
        return userService.getUserByUsername(username);
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Update a user")
    public UserResponse updateUser(@PathParam("id") UUID id, @Valid UserRequest request) {
        return userService.updateUser(id, request);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Delete a user")
    public Response deleteUser(@PathParam("id") UUID id) {
        userService.deleteUser(id);
        return Response.noContent().build();
    }
}
