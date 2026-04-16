package com.game.dataservice.controller.app;

import com.game.dataservice.entity.User;
import com.game.dataservice.repository.UserRepository;
import com.game.dataservice.service.WechatAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/app/user")
@RequiredArgsConstructor
@Tag(name = "App User & Auth", description = "WeChat Miniapp Login and User Management")
public class AppUserController {

    private final WechatAuthService wechatAuthService;
    private final UserRepository userRepository;

    @Operation(summary = "WeChat Miniapp Login", description = "Login using wx.login() code")
    @PostMapping("/wx-login")
    public ResponseEntity<Map<String, Object>> wxLogin(@RequestBody WxLoginRequest request) {
        Map<String, Object> result = wechatAuthService.login(request.getCode());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get Current User Info", description = "Get details of the currently authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = (String) auth.getPrincipal();
        
        return userRepository.findById(Long.parseLong(userIdStr))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update User Profile", description = "Update nickname, avatar, province, and city")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = (String) auth.getPrincipal();
        
        User updatedUser = wechatAuthService.updateUserInfo(
                Long.parseLong(userIdStr), 
                request.getNickname(), 
                request.getAvatarUrl(),
                request.getProvince(),
                request.getCity()
        );
        return ResponseEntity.ok(updatedUser);
    }

    @Data
    public static class WxLoginRequest {
        private String code;
    }

    @Data
    public static class UpdateProfileRequest {
        private String nickname;
        private String avatarUrl;
        private String province;
        private String city;
    }
}
