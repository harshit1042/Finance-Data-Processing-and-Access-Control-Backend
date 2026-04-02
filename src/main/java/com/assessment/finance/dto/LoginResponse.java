package com.assessment.finance.dto;

import com.assessment.finance.model.Role;

public record LoginResponse(String accessToken, String tokenType, long expiresInMs, String username, Role role) {}
