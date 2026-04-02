package com.assessment.finance.service;

import com.assessment.finance.dto.*;
import com.assessment.finance.exception.ApiException;
import com.assessment.finance.model.User;
import com.assessment.finance.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse create(CreateUserRequest request) {
        userRepository
                .findByUsername(request.username().trim())
                .ifPresent(u -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
                });
        User u = new User();
        u.setUsername(request.username().trim());
        u.setPasswordHash(passwordEncoder.encode(request.password()));
        u.setRole(request.role());
        u.setStatus(request.status());
        return toResponse(userRepository.save(u));
    }

    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse update(Long id, UpdateUserRequest request) {
        User u = userRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        u.setRole(request.role());
        u.setStatus(request.status());
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
        return toResponse(userRepository.save(u));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getRole(), u.getStatus(), u.getCreatedAt());
    }
}
