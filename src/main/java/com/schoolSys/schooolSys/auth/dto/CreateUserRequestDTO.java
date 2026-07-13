package com.schoolSys.schooolSys.auth.dto;

import com.schoolSys.schooolSys.auth.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequestDTO {
    @NotBlank @Email
    private String email;
    private String password;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotNull
    private UserRole role;
    private String tenantId;
}
