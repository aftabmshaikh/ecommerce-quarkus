package com.ecommerce.inventory.exception.mapper;

import com.ecommerce.inventory.exception.InventoryItemNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class InventoryItemNotFoundExceptionMapper implements ExceptionMapper<InventoryItemNotFoundException> {

    @Override
    public Response toResponse(InventoryItemNotFoundException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", Response.Status.NOT_FOUND.getStatusCode());
        body.put("error", "Not Found");
        body.put("message", exception.getMessage());
        body.put("path", "/"); // Placeholder

        return Response.status(Response.Status.NOT_FOUND).entity(body).build();
    }
}
