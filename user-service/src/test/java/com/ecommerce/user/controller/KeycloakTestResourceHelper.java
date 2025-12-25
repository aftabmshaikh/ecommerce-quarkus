package com.ecommerce.user.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Helper class to configure Keycloak client from test methods.
 * Extracted from KeycloakTestResource for reuse.
 */
class KeycloakTestResourceHelper {
    
    private static final String ADMIN_CLIENT_ID = "admin-cli";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    public void configureClient(String baseUrl, String realm, String clientId) {
        try {
            // Get admin token
            String adminToken = getAdminToken(baseUrl);
            if (adminToken == null) {
                System.err.println("KeycloakTestResourceHelper: Could not get admin token");
                return;
            }
            
            // Wait for client and configure it
            waitForClientAndConfigure(baseUrl, realm, clientId, adminToken);
        } catch (Exception e) {
            System.err.println("KeycloakTestResourceHelper: Error configuring client: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String getAdminToken(String baseUrl) {
        try {
            // Try password grant first
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
                System.err.println("KeycloakTestResourceHelper: Failed to get admin token via password grant. Status: " + response.statusCode() + ", Body: " + response.body());
                
                // Try client credentials grant as fallback
                System.out.println("KeycloakTestResourceHelper: Trying client credentials grant...");
                String clientCredentialsBody = "grant_type=client_credentials" +
                        "&client_id=" + ADMIN_CLIENT_ID;
                
                HttpRequest ccRequest = HttpRequest.newBuilder()
                        .uri(URI.create(tokenUrl))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(clientCredentialsBody))
                        .timeout(Duration.ofSeconds(10))
                        .build();
                
                HttpResponse<String> ccResponse = httpClient.send(ccRequest, HttpResponse.BodyHandlers.ofString());
                if (ccResponse.statusCode() != 200) {
                    System.err.println("KeycloakTestResourceHelper: Failed to get admin token via client credentials. Status: " + ccResponse.statusCode() + ", Body: " + ccResponse.body());
                    return null;
                }
                response = ccResponse;
            }

            // Parse JSON response to get access_token
            String body = response.body();
            System.out.println("KeycloakTestResourceHelper: Got admin token response (first 100 chars): " + body.substring(0, Math.min(100, body.length())));
            
            int tokenStart = body.indexOf("\"access_token\":\"") + 17;
            int tokenEnd = body.indexOf("\"", tokenStart);
            if (tokenStart > 16 && tokenEnd > tokenStart) {
                String token = body.substring(tokenStart, tokenEnd);
                System.out.println("KeycloakTestResourceHelper: Successfully extracted admin token (length: " + token.length() + ")");
                return token;
            }
            System.err.println("KeycloakTestResourceHelper: Could not parse access_token from response: " + body);
            return null;
        } catch (Exception e) {
            System.err.println("KeycloakTestResourceHelper: Error getting admin token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private void waitForClientAndConfigure(String baseUrl, String realm, String clientId, String adminToken) {
        int maxAttempts = 90;
        int attempt = 0;
        
        System.out.println("KeycloakTestResourceHelper: Waiting for client " + clientId + " to be created...");
        
        while (attempt < maxAttempts) {
            try {
                // Refresh admin token every 10 attempts in case it expired
                if (attempt > 0 && attempt % 10 == 0) {
                    System.out.println("KeycloakTestResourceHelper: Refreshing admin token...");
                    String newToken = getAdminToken(baseUrl);
                    if (newToken != null) {
                        adminToken = newToken;
                        System.out.println("KeycloakTestResourceHelper: Admin token refreshed");
                    } else {
                        System.err.println("KeycloakTestResourceHelper: Failed to refresh admin token");
                    }
                }
                
                if (configureClient(baseUrl, realm, clientId, adminToken)) {
                    System.out.println("KeycloakTestResourceHelper: Successfully configured client after " + attempt + " attempts");
                    return;
                }
                if (attempt % 10 == 0) {
                    System.out.println("KeycloakTestResourceHelper: Client not found yet, attempt " + attempt + "/" + maxAttempts);
                }
                Thread.sleep(1000);
                attempt++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                System.err.println("KeycloakTestResourceHelper: Error: " + e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
                attempt++;
            }
        }
        
        System.err.println("KeycloakTestResourceHelper: Timeout - Client " + clientId + " was not found after " + maxAttempts + " seconds");
    }
    
    private boolean configureClient(String baseUrl, String realm, String clientId, String adminToken) {
        try {
            String clientUrl = baseUrl + "/admin/realms/" + realm + "/clients";
            
            HttpRequest listRequest = HttpRequest.newBuilder()
                    .uri(URI.create(clientUrl))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> listResponse = httpClient.send(listRequest, HttpResponse.BodyHandlers.ofString());
            if (listResponse.statusCode() != 200) {
                System.err.println("KeycloakTestResourceHelper: Failed to list clients. Status: " + listResponse.statusCode() + ", Body: " + listResponse.body());
                return false;
            }

            String clientsJson = listResponse.body();
            
            // Debug: Print all client IDs to see what exists
            System.out.println("KeycloakTestResourceHelper: All clients in realm: " + clientsJson);
            
            String clientUuid = extractClientUuid(clientsJson, clientId);
            
            if (clientUuid == null) {
                System.out.println("KeycloakTestResourceHelper: Client " + clientId + " not found. Available clients: " + extractAllClientIds(clientsJson));
                System.out.println("KeycloakTestResourceHelper: Creating client " + clientId + " with password grant enabled...");
                // Try to create the client
                if (createClient(baseUrl, realm, clientId, adminToken)) {
                    // Client created, now get its UUID and configure it
                    HttpResponse<String> listResponse2 = httpClient.send(listRequest, HttpResponse.BodyHandlers.ofString());
                    if (listResponse2.statusCode() == 200) {
                        clientsJson = listResponse2.body();
                        clientUuid = extractClientUuid(clientsJson, clientId);
                    }
                }
                
                if (clientUuid == null) {
                    System.err.println("KeycloakTestResourceHelper: Failed to create or find client " + clientId);
                    return false;
                }
            }

            System.out.println("KeycloakTestResourceHelper: Found client " + clientId + " with UUID: " + clientUuid);
            
            String getClientUrl = baseUrl + "/admin/realms/" + realm + "/clients/" + clientUuid;
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getClientUrl))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            if (getResponse.statusCode() != 200) {
                return false;
            }

            String clientJson = getResponse.body();
            String updatedClientJson = updateClientJson(clientJson, true, true);
            
            HttpRequest updateRequest = HttpRequest.newBuilder()
                    .uri(URI.create(getClientUrl))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(updatedClientJson))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> updateResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());
            if (updateResponse.statusCode() == 204 || updateResponse.statusCode() == 200) {
                System.out.println("KeycloakTestResourceHelper: Successfully configured client " + clientId);
                return true;
            } else {
                System.err.println("KeycloakTestResourceHelper: Failed to update client. Status: " + updateResponse.statusCode());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("KeycloakTestResourceHelper: Error configuring client: " + e.getMessage());
            return false;
        }
    }
    
    private String extractClientUuid(String clientsJson, String clientId) {
        int startIdx = clientsJson.indexOf("\"clientId\":\"" + clientId + "\"");
        if (startIdx == -1) {
            return null;
        }
        
        int idStart = clientsJson.lastIndexOf("\"id\":\"", startIdx);
        if (idStart == -1) {
            return null;
        }
        
        int uuidStart = idStart + 6;
        int uuidEnd = clientsJson.indexOf("\"", uuidStart);
        if (uuidEnd == -1) {
            return null;
        }
        
        return clientsJson.substring(uuidStart, uuidEnd);
    }
    
    private String extractAllClientIds(String clientsJson) {
        // Extract all clientId values from the JSON array
        StringBuilder sb = new StringBuilder();
        int startIdx = 0;
        while ((startIdx = clientsJson.indexOf("\"clientId\":\"", startIdx)) != -1) {
            int clientIdStart = startIdx + 12; // Skip "clientId":"
            int clientIdEnd = clientsJson.indexOf("\"", clientIdStart);
            if (clientIdEnd != -1) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(clientsJson.substring(clientIdStart, clientIdEnd));
            }
            startIdx = clientIdEnd;
        }
        return sb.length() > 0 ? sb.toString() : "none";
    }
    
    private String updateClientJson(String clientJson, boolean enableDirectAccessGrants, boolean makePublic) {
        if (clientJson.contains("\"directAccessGrantsEnabled\"")) {
            clientJson = clientJson.replaceAll("\"directAccessGrantsEnabled\":\\s*false", "\"directAccessGrantsEnabled\": true");
            clientJson = clientJson.replaceAll("\"directAccessGrantsEnabled\":\\s*true", "\"directAccessGrantsEnabled\": true");
        } else {
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
        
        if (clientJson.contains("\"publicClient\"")) {
            clientJson = clientJson.replaceAll("\"publicClient\":\\s*false", "\"publicClient\": true");
            clientJson = clientJson.replaceAll("\"publicClient\":\\s*true", "\"publicClient\": true");
        } else {
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
    
    private boolean createClient(String baseUrl, String realm, String clientId, String adminToken) {
        try {
            String createClientUrl = baseUrl + "/admin/realms/" + realm + "/clients";
            
            // Create client JSON with password grant enabled
            String clientJson = "{" +
                    "\"clientId\":\"" + clientId + "\"," +
                    "\"enabled\":true," +
                    "\"publicClient\":true," +
                    "\"directAccessGrantsEnabled\":true," +
                    "\"standardFlowEnabled\":true," +
                    "\"implicitFlowEnabled\":false," +
                    "\"serviceAccountsEnabled\":false" +
                    "}";
            
            HttpRequest createRequest = HttpRequest.newBuilder()
                    .uri(URI.create(createClientUrl))
                    .header("Authorization", "Bearer " + adminToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(clientJson))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
            if (createResponse.statusCode() == 201 || createResponse.statusCode() == 200) {
                System.out.println("KeycloakTestResourceHelper: Successfully created client " + clientId);
                return true;
            } else {
                System.err.println("KeycloakTestResourceHelper: Failed to create client. Status: " + createResponse.statusCode() + ", Body: " + createResponse.body());
                return false;
            }
        } catch (Exception e) {
            System.err.println("KeycloakTestResourceHelper: Error creating client: " + e.getMessage());
            return false;
        }
    }
}

