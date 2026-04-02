package com.assessment.finance.service;

import com.assessment.finance.dto.LoginRequest;
import com.assessment.finance.dto.LoginResponse;
import com.assessment.finance.exception.ApiException;
import com.assessment.finance.model.User;
import com.assessment.finance.repository.UserRepository;
import com.assessment.finance.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long expirationMs;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.expirationMs = expirationMs;
    }

    public LoginResponse login(LoginRequest req) {
        String username = req.username() == null ? "" : req.username().trim();
        String password = req.password() == null ? "" : req.password();
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        if (user.getStatus() != com.assessment.finance.model.UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User is inactive");
        }
        String token = jwtService.createToken(user);
        return new LoginResponse(token, "Bearer", expirationMs, user.getUsername(), user.getRole());
    }
}
