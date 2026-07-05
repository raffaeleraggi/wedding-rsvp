package org.wedding.rsvp.controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    private String currentToken;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        if (!adminUsername.equals(request.getUsername()) ||
                !adminPassword.equals(request.getPassword())) {
            throw new IllegalArgumentException("Credenziali non valide");
        }

        currentToken = UUID.randomUUID().toString();

        return Map.of("token", currentToken);
    }

    @GetMapping("/check")
    public Map<String, Boolean> check(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        return Map.of("valid", token != null && token.equals(currentToken));
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
