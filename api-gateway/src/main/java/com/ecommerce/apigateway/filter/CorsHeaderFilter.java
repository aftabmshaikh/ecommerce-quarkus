package com.ecommerce.apigateway.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class CorsHeaderFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        requestContext.getHeaders().remove("Origin");
        requestContext.getHeaders().remove("Access-Control-Request-Method");
        requestContext.getHeaders().remove("Access-Control-Request-Headers");
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().remove("Access-Control-Allow-Origin");
        responseContext.getHeaders().remove("Access-Control-Allow-Credentials");
        responseContext.getHeaders().remove("Access-Control-Allow-Methods");
        responseContext.getHeaders().remove("Access-Control-Allow-Headers");
    }
}
