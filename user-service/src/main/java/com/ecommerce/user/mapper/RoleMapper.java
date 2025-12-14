package com.ecommerce.user.mapper;

import com.ecommerce.user.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "cdi")
public interface RoleMapper {
    Role toEntity(String roleName);

    String toName(Role role);
}
