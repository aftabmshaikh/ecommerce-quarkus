package com.ecommerce.inventory.exception.mapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", Response.Status.BAD_REQUEST.getStatusCode());
        body.put("error", "Constraint Violation");

        List<String> errors = exception.getConstraintViolations().stream()
                .map(violation -> String.format("%s: %s",
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .collect(Collectors.toList());

        body.put("message", "Validation failed");
        body.put("errors", errors);
        body.put("path", "/"); // Placeholder

        return Response.status(Response.Status.BAD_REQUEST).entity(body).build();
    }
}
