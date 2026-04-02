package com.assessment.finance.dto;

import com.assessment.finance.model.Role;
import com.assessment.finance.model.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "username is required") String username,
        @NotBlank(message = "password is required")
                @Size(min = 8, max = 72, message = "password must be 8–72 characters")
                String password,
        @NotNull(message = "role is required") Role role,
        @NotNull(message = "status is required") UserStatus status) {}
