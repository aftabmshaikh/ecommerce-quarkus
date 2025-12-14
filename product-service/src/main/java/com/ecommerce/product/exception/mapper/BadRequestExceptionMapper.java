package com.ecommerce.product.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<RuntimeException> { // Handles IllegalArgumentException and IllegalStateException

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof IllegalArgumentException || exception instanceof IllegalStateException) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", Response.Status.BAD_REQUEST.getStatusCode());
            body.put("error", "Bad Request");
            body.put("message", exception.getMessage());
            body.put("path", "/"); // Placeholder

            return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
        }
        return null; // Let other mappers handle other RuntimeExceptions
    }
}
