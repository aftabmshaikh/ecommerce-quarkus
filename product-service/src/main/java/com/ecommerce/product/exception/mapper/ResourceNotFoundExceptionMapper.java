package com.ecommerce.product.exception.mapper;

import com.ecommerce.product.exception.ResourceNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", Response.Status.NOT_FOUND.getStatusCode());
        body.put("error", "Not Found");
        body.put("message", exception.getMessage());
        // In a real application, you might extract the path from the request context
        body.put("path", "/"); 

        return Response.status(Response.Status.NOT_FOUND).entity(body).build();
    }
}
