package com.ecommerce.inventory.exception.mapper;

import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class OptimisticLockExceptionMapper implements ExceptionMapper<OptimisticLockException> {

    @Override
    public Response toResponse(OptimisticLockException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", Response.Status.CONFLICT.getStatusCode());
        body.put("error", "Optimistic Locking Failure");
        body.put("message", "The data was modified by another transaction. Please refresh and try again.");
        body.put("path", "/"); // Placeholder

        return Response.status(Response.Status.CONFLICT).entity(body).build();
    }
}
