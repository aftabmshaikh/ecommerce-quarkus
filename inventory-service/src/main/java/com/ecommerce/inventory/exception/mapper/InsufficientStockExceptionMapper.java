package com.ecommerce.inventory.exception.mapper;

import com.ecommerce.inventory.exception.InsufficientStockException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class InsufficientStockExceptionMapper implements ExceptionMapper<InsufficientStockException> {

    @Override
    public Response toResponse(InsufficientStockException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", Response.Status.CONFLICT.getStatusCode());
        body.put("error", "Conflict");
        body.put("message", exception.getMessage());
        body.put("path", "/"); // Placeholder

        return Response.status(Response.Status.CONFLICT).entity(body).build();
    }
}
