package com.apishield.apishield.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.apishield.apishield.dto.LoginRequest;
import com.apishield.apishield.dto.LoginResponse;
import com.apishield.apishield.dto.UserRequest;
import com.apishield.apishield.dto.UserResponse;
import com.apishield.apishield.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User APIs", description = "APIs for user registration, authentication and user management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Register user - Public
    @Operation(summary = "Register a new user")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(
            @Valid @RequestBody UserRequest userRequest) {

        return userService.createUser(userRequest);
    }

    // Get all users - ADMIN only
    @GetMapping
    @Operation(summary = "Get all users")
    @SecurityRequirement(name = "bearerAuth")
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers();
    }

    // Get current logged-in user

    @Operation(summary = "Get currently logged-in user")
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public UserResponse getCurrentUser() {

        return userService.getCurrentUser();
    }

    // Get user by ID

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    @SecurityRequirement(name = "bearerAuth")
    public UserResponse getUserById(
            @PathVariable Long id) {

        return userService.getUserById(id);
    }

    // Update user
    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {

        return userService.updateUser(id, userRequest);
    }

    // Delete user - ADMIN only
    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    // Login - Public
    @Operation(summary = "Login and generate JWT")
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest loginRequest) {

        return userService.login(loginRequest);
    }
}