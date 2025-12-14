package com.ecommerce.user.service;

import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.model.Role;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class UserService {

    private static final Logger LOG = Logger.getLogger(UserService.class);

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    UserMapper userMapper;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(hashPassword(request.getPassword())); // Hash password
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
                roleRepository.findByName(roleName).ifPresent(roles::add);
            }
        } else {
            roleRepository.findByName("USER").ifPresent(roles::add); // Default role
        }
        user.setRoles(roles);

        userRepository.persist(user);
        LOG.infof("User created: %s", user.getUsername());
        return userMapper.toResponse(user);
    }

    public UserResponse getUserById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return userMapper.toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.listAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        userMapper.updateFromRequest(request, user);
        user.setUpdatedAt(LocalDateTime.now());

        if (request.getRoles() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                roleRepository.findByName(roleName).ifPresent(roles::add);
            }
            user.setRoles(roles);
        }
        
        userRepository.persist(user); // Panache automatically merges changes
        LOG.infof("User updated: %s", user.getUsername());
        return userMapper.toResponse(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        User user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
        LOG.infof("User deleted: %s", user.getUsername());
    }

    @Transactional
    public void updateLastLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.persist(user);
        });
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        // In a real application, use a proper password encoder (e.g., BCrypt)
        return hashPassword(rawPassword).equals(encodedPassword);
    }

    private String hashPassword(String password) {
        // Placeholder for password hashing. In a real app, use a secure hashing algorithm.
        return "{noop}" + password; // {noop} is for Spring Security's NoOpPasswordEncoder
    }
}
