package com.ecommerce.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private UUID id;
    private String type; // SHIPPING, BILLING, BOTH
    private String firstName;
    private String lastName;
    private String company;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String phone;
    private boolean isDefault;
}

