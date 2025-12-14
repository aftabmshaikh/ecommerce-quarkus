package com.ecommerce.apigateway.config;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpSecurityPolicy;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
@IfBuildProfile("minimal")
public class SecurityConfig {

    @Produces
    @Named("security-policy")
    public HttpSecurityPolicy securityPolicy() {
        return new HttpSecurityPolicy() {
            @Override
            public Uni<CheckResult> checkPermission(RoutingContext request, 
                                                 Uni<SecurityIdentity> identity, 
                                                 AuthorizationRequestContext context) {
                String path = request.request().path();
                // Permit all for health checks, registration, login, and product browsing
                if (path.startsWith("/actuator/health") ||
                    path.startsWith("/actuator/info") ||
                    path.startsWith("/api/users/register") ||
                    path.startsWith("/api/users/login") ||
                    path.startsWith("/api/products")) {
                    return Uni.createFrom().item(CheckResult.PERMIT);
                }
                // For all other paths, require authentication
                return identity.onItem().transform(i -> i != null ? CheckResult.PERMIT : CheckResult.DENY);
            }
        };
    }
}
