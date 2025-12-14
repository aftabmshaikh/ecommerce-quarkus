package com.ecommerce.user.service;

import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.dto.LoginResponse;
import com.ecommerce.user.dto.UserRequest;
import com.ecommerce.user.dto.UserResponse;
import com.ecommerce.user.exception.AuthenticationException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    @Inject
    UserService userService;

    @Inject
    UserRepository userRepository;

    @Inject
    JwtService jwtService;

    @Transactional
    public UserResponse register(UserRequest request) {
        // UserService already handles validation and password hashing
        return userService.createUser(request);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getIdentifier());
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(request.getIdentifier());
        }

        User user = userOptional.orElseThrow(() -> new UserNotFoundException("Invalid credentials"));

        if (!userService.verifyPassword(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.persist(user); // Update last login time

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user); // Assuming refresh token logic

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(userService.getUserById(user.getId()))
                .build();
    }

    // Method to validate JWT token (can be used by other services or for token refresh)
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
}
