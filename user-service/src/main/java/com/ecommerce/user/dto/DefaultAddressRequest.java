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
public class DefaultAddressRequest {
    @NotBlank(message = "Type is required")
    private String type; // "shipping" or "billing"
}

