package com.ecommerce.user.repository;

import com.ecommerce.user.model.Role;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RoleRepository implements PanacheRepositoryBase<Role, UUID> {
    public Optional<Role> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}
