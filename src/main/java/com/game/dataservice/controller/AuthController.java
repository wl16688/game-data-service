package com.game.dataservice.controller;

import com.game.dataservice.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Mock endpoints to generate JWT tokens")
public class AuthController {

    private final JwtUtils jwtUtils;

    @Operation(summary = "Login as App User", description = "Returns a JWT token with USER role")
    @PostMapping("/login/user")
    public ResponseEntity<?> loginUser(@RequestParam String username) {
        String token = jwtUtils.generateToken(username, "USER");
        return ResponseEntity.ok(Map.of("token", token, "role", "USER"));
    }

    @Operation(summary = "Login as Admin", description = "Returns a JWT token with ADMIN role")
    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestParam String username) {
        String token = jwtUtils.generateToken(username, "ADMIN");
        return ResponseEntity.ok(Map.of("token", token, "role", "ADMIN"));
    }
}
