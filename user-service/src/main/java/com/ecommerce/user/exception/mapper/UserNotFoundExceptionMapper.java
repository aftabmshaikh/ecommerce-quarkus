package com.ecommerce.user.exception.mapper;

import com.ecommerce.user.exception.UserNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class UserNotFoundExceptionMapper implements ExceptionMapper<UserNotFoundException> {

    @Override
    public Response toResponse(UserNotFoundException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", Response.Status.NOT_FOUND.getStatusCode());
        body.put("error", "Not Found");
        body.put("message", exception.getMessage());
        body.put("path", "/");

        return Response.status(Response.Status.NOT_FOUND).entity(body).build();
    }
}

