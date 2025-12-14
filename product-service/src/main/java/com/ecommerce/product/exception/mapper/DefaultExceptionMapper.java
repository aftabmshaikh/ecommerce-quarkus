package com.ecommerce.product.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(DefaultExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        LOG.error("Unhandled exception occurred: ", exception);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred: " + exception.getMessage());
        body.put("path", "/"); // Placeholder

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }
}
