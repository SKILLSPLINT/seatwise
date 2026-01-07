package com.seatwise.user_service.dto.response;

import enums.EGender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private EGender gender;
    private Set<String> roles;
    private String profileImageUrl;
    private String createdAt;  // Timezone-converted string
    private String updatedAt;  // Timezone-converted string
}

