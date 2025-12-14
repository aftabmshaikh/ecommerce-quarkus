package com.ecommerce.notification.controller;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.service.NotificationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notification API", description = "APIs for managing notifications")
public class NotificationController {

    @Inject
    NotificationService notificationService;

    @GET
    @Path("/pending/count")
    @Operation(summary = "Get the count of pending notifications for an email")
    public Response getPendingNotificationCount(@QueryParam("email") String email) {
        long count = notificationService.getPendingNotificationCount(email);
        return Response.ok(Map.of("count", count)).build();
    }

    @POST
    @Operation(summary = "Send an email notification")
    public Response sendEmail(
            @QueryParam("to") String to,
            @QueryParam("subject") String subject,
            @QueryParam("content") String content,
            @QueryParam("type") Notification.NotificationType type) {
        
        notificationService.sendEmailNotification(to, subject, content, type);
        return Response.accepted().build();
    }
}
