package com.ecommerce.user.controller;

import com.ecommerce.user.dto.*;
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

    // Admin endpoints
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

    // Current user endpoints (matching Angular frontend expectations)
    @GET
    @Path("/me")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Get current user profile")
    public UserResponse getCurrentUser() {
        // TODO: Extract user ID from security context
        // For now, this is a placeholder - should get current user from JWT token
        UUID currentUserId = getCurrentUserId(); // Placeholder
        return userService.getUserById(currentUserId);
    }

    @PATCH
    @Path("/me")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Update current user profile")
    public UserResponse updateCurrentUser(@Valid UserProfileRequest request) {
        // TODO: Extract user ID from security context
        UUID currentUserId = getCurrentUserId(); // Placeholder
        UserRequest userRequest = convertToUserRequest(request);
        return userService.updateUser(currentUserId, userRequest);
    }

    @POST
    @Path("/change-password")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Change user password")
    public Response changePassword(@Valid ChangePasswordRequest request) {
        // TODO: Implement password change logic
        // Should verify current password, validate new password, and update
        return Response.ok().build();
    }

    // Address endpoints
    @GET
    @Path("/me/addresses")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Get current user addresses")
    public Response getAddresses() {
        // TODO: Implement address retrieval
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Address management not yet implemented\"}")
                .build();
    }

    @POST
    @Path("/me/addresses")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Add address to current user")
    public Response addAddress(@Valid AddressRequest request) {
        // TODO: Implement address creation
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Address management not yet implemented\"}")
                .build();
    }

    @PATCH
    @Path("/me/addresses/{addressId}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Update address")
    public Response updateAddress(@PathParam("addressId") UUID addressId, @Valid AddressRequest request) {
        // TODO: Implement address update
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Address management not yet implemented\"}")
                .build();
    }

    @DELETE
    @Path("/me/addresses/{addressId}")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Delete address")
    public Response deleteAddress(@PathParam("addressId") UUID addressId) {
        // TODO: Implement address deletion
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Address management not yet implemented\"}")
                .build();
    }

    @POST
    @Path("/me/addresses/{addressId}/default")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Set default address")
    public Response setDefaultAddress(@PathParam("addressId") UUID addressId, DefaultAddressRequest request) {
        // TODO: Implement set default address
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Address management not yet implemented\"}")
                .build();
    }

    // Avatar endpoints
    @POST
    @Path("/me/avatar")
    @RolesAllowed({"ADMIN", "USER"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Upload user avatar")
    public Response uploadAvatar(@FormParam("file") java.io.InputStream file) {
        // TODO: Implement avatar upload
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Avatar upload not yet implemented\"}")
                .build();
    }

    @DELETE
    @Path("/me/avatar")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Delete user avatar")
    public Response deleteAvatar() {
        // TODO: Implement avatar deletion
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Avatar deletion not yet implemented\"}")
                .build();
    }

    // Verification endpoints
    @POST
    @Path("/verify-email/request")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Request email verification")
    public Response requestEmailVerification() {
        // TODO: Implement email verification request
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Email verification not yet implemented\"}")
                .build();
    }

    @POST
    @Path("/verify-email/confirm")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Confirm email verification")
    public Response confirmEmailVerification(@Valid VerificationRequest request) {
        // TODO: Implement email verification confirmation
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Email verification not yet implemented\"}")
                .build();
    }

    @POST
    @Path("/verify-phone/request")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Request phone verification")
    public Response requestPhoneVerification(@Valid VerificationRequest request) {
        // TODO: Implement phone verification request
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Phone verification not yet implemented\"}")
                .build();
    }

    @POST
    @Path("/verify-phone/confirm")
    @RolesAllowed({"ADMIN", "USER"})
    @Operation(summary = "Confirm phone verification")
    public Response confirmPhoneVerification(@Valid VerificationRequest request) {
        // TODO: Implement phone verification confirmation
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Phone verification not yet implemented\"}")
                .build();
    }

    // Helper methods
    private UUID getCurrentUserId() {
        // TODO: Extract from JWT token or security context
        // This is a placeholder - should get from authenticated user
        return UUID.randomUUID(); // Placeholder
    }

    private UserRequest convertToUserRequest(UserProfileRequest request) {
        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName(request.getFirstName());
        userRequest.setLastName(request.getLastName());
        userRequest.setPhoneNumber(request.getPhoneNumber());
        userRequest.setAddress(request.getAddress());
        userRequest.setCity(request.getCity());
        userRequest.setState(request.getState());
        userRequest.setZipCode(request.getZipCode());
        userRequest.setCountry(request.getCountry());
        return userRequest;
    }
}
