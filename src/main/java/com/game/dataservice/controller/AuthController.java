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
@Tag(name = "认证", description = "用于获取 JWT Token 的模拟登录接口")
public class AuthController {

    private final JwtUtils jwtUtils;

    @Operation(summary = "小程序用户登录", description = "返回带 USER 角色的 JWT Token")
    @PostMapping("/login/user")
    public ResponseEntity<?> loginUser(@RequestParam String username) {
        String token = jwtUtils.generateToken(username, "USER");
        return ResponseEntity.ok(Map.of("token", token, "role", "USER"));
    }

    @Operation(summary = "管理员登录", description = "返回带 ADMIN 角色的 JWT Token")
    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestParam String username) {
        String token = jwtUtils.generateToken(username, "ADMIN");
        return ResponseEntity.ok(Map.of("token", token, "role", "ADMIN"));
    }
}
