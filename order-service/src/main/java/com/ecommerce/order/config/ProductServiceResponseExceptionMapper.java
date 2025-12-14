package com.ecommerce.order.config;

import com.ecommerce.order.exception.ProductServiceException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Provider
public class ProductServiceResponseExceptionMapper implements ResponseExceptionMapper<ProductServiceException> {

    @Override
    public ProductServiceException toThrowable(Response response) {
        String message = "Error from Product Service: " + response.getStatus() + " - " + response.getStatusInfo().getReasonPhrase();
        if (response.hasEntity()) {
            try {
                message += " Body: " + response.readEntity(String.class);
            } catch (Exception e) {
                // Ignore if body cannot be read
            }
        }
        return new ProductServiceException(message);
    }
}
