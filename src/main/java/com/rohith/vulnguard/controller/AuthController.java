package com.rohith.vulnguard.controller;

import com.rohith.vulnguard.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Authentication", description = "Register and login to obtain JWT tokens")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
               description = "Creates a new account with ROLE_USER.")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(
                request.getUsername(), request.getEmail(), request.getPassword()));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token",
               description = "Returns a Bearer token. Use it in the Authorize dialog above.")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(
                request.getUsername(), request.getPassword()));
    }

    // ── DTOs ──────────────────────────────────────────────────────────

    public static class RegisterRequest {

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        public String getUsername() { return username; }
        public String getEmail()    { return email; }
        public String getPassword() { return password; }

        public void setUsername(String username) { this.username = username; }
        public void setEmail(String email)       { this.email = email; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {

        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;

        public String getUsername() { return username; }
        public String getPassword() { return password; }

        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
    }
}
