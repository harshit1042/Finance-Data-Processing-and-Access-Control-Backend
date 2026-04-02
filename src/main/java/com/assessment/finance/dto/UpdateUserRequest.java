package com.assessment.finance.dto;

import com.assessment.finance.model.Role;
import com.assessment.finance.model.UserStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotNull(message = "role is required") Role role,
        @NotNull(message = "status is required") UserStatus status,
        @Size(min = 8, max = 72, message = "newPassword must be 8–72 characters when provided")
                String newPassword) {}
