package com.schoolSys.schooolSys.auth.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.auth.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String tenantId;
    private String tenantSlug;
    private Boolean isActive;
}
