package com.ecommerce.user.controller;

import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.dto.LoginResponse;
import com.ecommerce.user.dto.RefreshTokenRequest;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.service.AuthService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication API", description = "APIs for user authentication")
public class AuthController {

    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    @Operation(summary = "Register a new user")
    public Response register(@Valid UserRequest request) {
        UserResponse response = authService.register(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/login")
    @Operation(summary = "Authenticate a user and get a token")
    public LoginResponse login(@Valid LoginRequest request) {
        return authService.login(request);
    }

    @POST
    @Path("/refresh-token")
    @Operation(summary = "Refresh authentication token")
    public LoginResponse refreshToken(@Valid RefreshTokenRequest request) {
        return authService.refreshToken(request.getToken());
    }
}
