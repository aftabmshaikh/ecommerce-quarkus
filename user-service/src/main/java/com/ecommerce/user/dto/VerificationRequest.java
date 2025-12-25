package com.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequest {
    @NotBlank(message = "Token/Code is required")
    private String token; // For email verification
    private String code; // For phone verification
    private String phone; // For phone verification request
}

