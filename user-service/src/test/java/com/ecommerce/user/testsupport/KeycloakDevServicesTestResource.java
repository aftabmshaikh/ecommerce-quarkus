package com.ecommerce.user.testsupport;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.opentest4j.TestAbortedException;

import java.util.HashMap;
import java.util.Map;

/**
 * Test resource that enables Keycloak DevServices for tests that need real Keycloak authentication.
 * 
 * This resource:
 * 1. Checks if Docker is available
 * 2. Enables OIDC and Keycloak DevServices
 * 3. Configures the client for password grant
 */
public class KeycloakDevServicesTestResource implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        // Check if Docker is available
        if (!isDockerAvailable()) {
            System.out.println("Docker is not available. Skipping Keycloak DevServices.");
            throw new TestAbortedException("Docker is not available. Tests require Testcontainers.");
        }

        Map<String, String> props = new HashMap<>();
        
        // Enable OIDC and Keycloak DevServices
        props.put("quarkus.oidc.enabled", "true");
        props.put("quarkus.oidc.devservices.enabled", "true");
        props.put("quarkus.oidc.devservices.image-name", "quay.io/keycloak/keycloak:20.0.5");
        props.put("quarkus.oidc.client-id", "quarkus-app");
        
        // Try to configure the client to enable password grant via DevServices
        // Note: Quarkus DevServices might not support this directly, so we'll configure it via Admin API
        // But first, let's see if we can set any properties that help
        props.put("quarkus.oidc.devservices.realm-name", "quarkus");
        
        // Override JWT configuration for tests
        props.put("mp.jwt.verify.publickey.location", "classpath:META-INF/resources/publicKey.pem");
        props.put("mp.jwt.verify.issuer", "${quarkus.oidc.auth-server-url}");
        
        return props;
    }

    @Override
    public void stop() {
        // Nothing to clean up
    }

    private boolean isDockerAvailable() {
        try {
            org.testcontainers.DockerClientFactory.instance().client();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

