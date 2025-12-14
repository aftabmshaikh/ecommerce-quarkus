package com.ecommerce.inventory.exception.mapper;

import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {

    @Override
    public Response toResponse(PersistenceException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", Response.Status.CONFLICT.getStatusCode());
        body.put("error", "Data Integrity Violation");
        body.put("message", "Operation could not be completed due to data integrity violation: " + exception.getMessage());
        body.put("path", "/"); // Placeholder

        return Response.status(Response.Status.CONFLICT).entity(body).build();
    }
}
