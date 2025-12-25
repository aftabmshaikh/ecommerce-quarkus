package com.ecommerce.user.testsupport;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.eclipse.microprofile.config.ConfigProvider;
import org.opentest4j.TestAbortedException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Custom Keycloak test resource that configures the Keycloak client to enable password grant.
 * 
 * This resource:
 * 1. Waits for Keycloak DevServices to be ready
 * 2. Gets an admin token from Keycloak
 * 3. Configures the "quarkus-app" client to enable "Direct Access Grants" (password grant)
 * 4. Ensures the client is public (no client secret required)
 */
public class KeycloakTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String REALM = "quarkus";
    private static final String CLIENT_ID = "quarkus-app";
    private static final String ADMIN_CLIENT_ID = "admin-cli";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public Map<String, String> start() {
        // Keycloak DevServices is started by KeycloakDevServicesTestResource
        // We need to wait for it to be ready and then configure the client
        System.out.println("KeycloakTestResource: Starting client configuration...");
        
        try {
            // Wait a bit for Keycloak DevServices to fully initialize
            Thread.sleep(5000);
            
            Optional<String> authServerUrl = ConfigProvider.getConfig()
                    .getOptionalValue("quarkus.oidc.auth-server-url", String.class);
            
            if (authServerUrl.isEmpty()) {
                System.out.println("KeycloakTestResource: auth-server-url not found yet. Will retry in test.");
                // Return empty - we'll configure in @BeforeAll
                return new HashMap<>();
            }

            String baseUrl = authServerUrl.get().replace("/realms/" + REALM, "");
            System.out.println("KeycloakTestResource: Configuring Keycloak client at: " + baseUrl);

            // Wait for Keycloak to be ready
            waitForKeycloak(baseUrl);
            System.out.println("KeycloakTestResource: Keycloak is ready");

            // Get admin token
            String adminToken = getAdminToken(baseUrl);
            if (adminToken == null) {
                System.out.println("KeycloakTestResource: Warning - Could not get admin token. Client configuration will be retried in test.");
                return new HashMap<>();
            }
            System.out.println("KeycloakTestResource: Got admin token");

            // Wait for the client to be created by DevServices, then configure it
            System.out.println("KeycloakTestResource: Waiting for client and configuring...");
            waitForClientAndConfigure(baseUrl, adminToken);
            System.out.println("KeycloakTestResource: Client configuration completed");

            return new HashMap<>();
        } catch (Exception e) {
            System.err.println("KeycloakTestResource: Warning - Failed to configure Keycloak client: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the tests - they can still use @TestSecurity
            return new HashMap<>();
        }
    }

    @Override
    public void stop() {
        // Nothing to clean up
    }

    private void waitForKeycloak(String baseUrl) throws InterruptedException {
        String healthUrl = baseUrl + "/health/ready";
        int maxAttempts = 30;
        int attempt = 0;

        while (attempt < maxAttempts) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(healthUrl))
                        .GET()
                        .timeout(Duration.ofSeconds(5))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    System.out.println("Keycloak is ready");
                    return;
                }
            } catch (Exception e) {
                // Keycloak not ready yet
            }
            attempt++;
            Thread.sleep(1000);
        }
        throw new RuntimeException("Keycloak did not become ready within 30 seconds");
    }

    private String getAdminToken(String baseUrl) {
        try {
            String tokenUrl = baseUrl + "/realms/master/protocol/openid-connect/token";
            String requestBody = "grant_type=password" +
                    "&client_id=" + ADMIN_CLIENT_ID +
                    "&username=" + ADMIN_USERNAME +
                    "&password=" + ADMIN_PASSWORD;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("Failed to get admin token. Status: " + response.statusCode() + ", Body: " + response.body());
                return null;
            }

            // Parse JSON response to get access_token
            String body = response.body();
            int tokenStart = body.indexOf("\"access_token\":\"") + 17;
            int tokenEnd = body.indexOf("\"", tokenStart);
            if (tokenStart > 16 && tokenEnd > tokenStart) {
                return body.substring(tokenStart, tokenEnd);
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting admin token: " + e.getMessage());
            return null;
        }
    }

    private void waitForClientAndConfigure(String baseUrl, String adminToken) {
        // Wait up to 90 seconds for the client to be created by DevServices
        int maxAttempts = 90;
        int attempt = 0;
        
        System.out.println("KeycloakTestResource: Waiting for client " + CLIENT_ID + " to be created...");
        
        while (attempt < maxAttempts) {
            try {
                if (configureClient(baseUrl, adminToken)) {
                    System.out.println("KeycloakTestResource: Successfully configured client after " + attempt + " attempts");
                    return; // Successfully configured
                }
                // Client not found yet, wait and retry
                if (attempt % 10 == 0) {
                    System.out.println("KeycloakTestResource: Client not found yet, attempt " + attempt + "/" + maxAttempts);
                }
                Thread.sleep(1000);
                attempt++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("KeycloakTestResource: Interrupted while waiting for client");
                return;
            } catch (Exception e) {
                System.err.println("KeycloakTestResource: Error while waiting for client: " + e.getMessage());
                // Continue retrying
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
                attempt++;
            }
        }
        
        System.err.println("KeycloakTestResource: Timeout - Client " + CLIENT_ID + " was not found after " + maxAttempts + " seconds");
    }

    /**
     * Configures the client to enable password grant.
     * @return true if configuration was successful, false if client not found
     */
    private boolean configureClient(String baseUrl, String adminToken) {
        try {
            // Get the client configuration
            String clientUrl = baseUrl + "/admin/realms/" + REALM + "/clients";
            
            HttpRequest listRequest = HttpRequest.newBuilder()
                    .uri(URI.create(clientUrl))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> listResponse = httpClient.send(listRequest, HttpResponse.BodyHandlers.ofString());
            if (listResponse.statusCode() != 200) {
                System.err.println("Failed to list clients. Status: " + listResponse.statusCode() + ", Body: " + listResponse.body());
                return false;
            }

            // Find the quarkus-app client and extract its UUID
            String clientsJson = listResponse.body();
            String clientUuid = extractClientUuid(clientsJson, CLIENT_ID);
            
            if (clientUuid == null) {
                // Client not found yet - return false to indicate retry needed
                return false;
            }

            System.out.println("Found client " + CLIENT_ID + " with UUID: " + clientUuid);
            
            // Get current client configuration
            String getClientUrl = baseUrl + "/admin/realms/" + REALM + "/clients/" + clientUuid;
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getClientUrl))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            if (getResponse.statusCode() != 200) {
                System.err.println("Failed to get client. Status: " + getResponse.statusCode() + ", Body: " + getResponse.body());
                return false;
            }

            String clientJson = getResponse.body();
            
            // Update client to enable direct access grants and make it public
            // We need to modify the JSON to set:
            // - "directAccessGrantsEnabled": true
            // - "publicClient": true (if not already)
            String updatedClientJson = updateClientJson(clientJson, true, true);
            
            // Update the client
            HttpRequest updateRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getClientUrl))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(updatedClientJson))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> updateResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());
            if (updateResponse.statusCode() == 204 || updateResponse.statusCode() == 200) {
                System.out.println("Successfully configured client " + CLIENT_ID + " to enable Direct Access Grants");
                return true;
            } else {
                System.err.println("Failed to update client. Status: " + updateResponse.statusCode() + ", Body: " + updateResponse.body());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error configuring client: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Extracts the client UUID from the clients JSON array by matching clientId.
     */
    private String extractClientUuid(String clientsJson, String clientId) {
        // Simple JSON parsing - find the client with matching clientId and extract its id
        // Format: [{"id":"uuid","clientId":"quarkus-app",...}, ...]
        int startIdx = clientsJson.indexOf("\"clientId\":\"" + clientId + "\"");
        if (startIdx == -1) {
            return null;
        }
        
        // Look backwards to find the "id" field before this clientId
        int idStart = clientsJson.lastIndexOf("\"id\":\"", startIdx);
        if (idStart == -1) {
            return null;
        }
        
        int uuidStart = idStart + 6; // Skip "id":"
        int uuidEnd = clientsJson.indexOf("\"", uuidStart);
        if (uuidEnd == -1) {
            return null;
        }
        
        return clientsJson.substring(uuidStart, uuidEnd);
    }

    /**
     * Updates the client JSON to enable direct access grants and set public client.
     */
    private String updateClientJson(String clientJson, boolean enableDirectAccessGrants, boolean makePublic) {
        // Replace or add directAccessGrantsEnabled
        if (clientJson.contains("\"directAccessGrantsEnabled\"")) {
            clientJson = clientJson.replaceAll("\"directAccessGrantsEnabled\":\\s*false", "\"directAccessGrantsEnabled\": true");
            clientJson = clientJson.replaceAll("\"directAccessGrantsEnabled\":\\s*true", "\"directAccessGrantsEnabled\": true");
        } else {
            // Insert after "clientId" or "id"
            int insertPos = clientJson.indexOf("\"clientId\"");
            if (insertPos == -1) {
                insertPos = clientJson.indexOf("\"id\"");
            }
            if (insertPos != -1) {
                int commaPos = clientJson.indexOf(",", insertPos);
                if (commaPos != -1) {
                    clientJson = clientJson.substring(0, commaPos + 1) + 
                                "\"directAccessGrantsEnabled\": true," + 
                                clientJson.substring(commaPos + 1);
                }
            }
        }
        
        // Replace or add publicClient
        if (clientJson.contains("\"publicClient\"")) {
            clientJson = clientJson.replaceAll("\"publicClient\":\\s*false", "\"publicClient\": true");
            clientJson = clientJson.replaceAll("\"publicClient\":\\s*true", "\"publicClient\": true");
        } else {
            // Insert after directAccessGrantsEnabled or clientId
            int insertPos = clientJson.indexOf("\"directAccessGrantsEnabled\"");
            if (insertPos == -1) {
                insertPos = clientJson.indexOf("\"clientId\"");
            }
            if (insertPos != -1) {
                int commaPos = clientJson.indexOf(",", insertPos);
                if (commaPos != -1) {
                    clientJson = clientJson.substring(0, commaPos + 1) + 
                                "\"publicClient\": true," + 
                                clientJson.substring(commaPos + 1);
                }
            }
        }
        
        return clientJson;
    }
}

