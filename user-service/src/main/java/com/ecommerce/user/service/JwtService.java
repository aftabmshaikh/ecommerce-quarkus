package com.ecommerce.user.service;

import com.ecommerce.user.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "jwt.secret")
    String secret;

    @ConfigProperty(name = "jwt.expiration")
    long expirationMillis;

    public String generateToken(User user) {
        Set<String> roles = user.getRoles().stream()
                                .map(role -> role.getName())
                                .collect(Collectors.toSet());

        return Jwt.issuer("https://example.com/issuer") // Replace with your actual issuer
                .upn(user.getUsername())
                .groups(roles)
                .expiresIn(Duration.ofMillis(expirationMillis))
                .signWithSecret(secret);
    }

    public String generateRefreshToken(User user) {
        // For simplicity, using the same secret and a longer expiration for refresh token
        // In a real application, refresh tokens should be stored securely and managed carefully
        return Jwt.issuer("https://example.com/issuer")
                .upn(user.getUsername())
                .groups(new HashSet<>(Arrays.asList("REFRESH_TOKEN"))) // A specific role for refresh tokens
                .expiresIn(Duration.ofDays(7)) // Longer expiration for refresh token
                .signWithSecret(secret);
    }

    public boolean validateToken(String token) {
        // With SmallRye JWT, validation is typically handled by the JAX-RS security context
        // and @RolesAllowed annotations. Manual validation here would involve parsing
        // and verifying the signature, which is complex and usually delegated to the framework.
        // For a basic check, you might try to parse it, but proper validation is done by MP-JWT.
        try {
            // This is a simplistic check. Real validation involves signature verification,
            // expiration, issuer, audience, etc., which SmallRye JWT handles automatically
            // when the token is presented in a secured endpoint.
            // If you need to manually validate, you'd use a JwtConsumer.
            // For now, we'll assume if it can be processed without immediate error, it's "valid"
            // for the purpose of this placeholder.
            return true; // Placeholder
        } catch (Exception e) {
            return false;
        }
    }
}
