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
@Tag(name = "小程序用户与认证", description = "微信小程序一键登录与用户资料管理")
public class AppUserController {

    private final WechatAuthService wechatAuthService;
    private final UserRepository userRepository;

    @Operation(summary = "微信小程序一键登录", description = "使用 wx.login() 获取的 code 换取 openid 并签发 JWT")
    @PostMapping("/wx-login")
    public ResponseEntity<Map<String, Object>> wxLogin(@RequestBody WxLoginRequest request) {
        Map<String, Object> result = wechatAuthService.login(request.getCode());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前已登录用户的资料信息")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = (String) auth.getPrincipal();
        
        return userRepository.findById(Long.parseLong(userIdStr))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "更新用户资料", description = "更新昵称、头像与行政区划（国家/省/市/区）ID")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = (String) auth.getPrincipal();
        
        User updatedUser = wechatAuthService.updateUserInfo(
                Long.parseLong(userIdStr), 
                request.getNickname(), 
                request.getAvatarUrl(),
                request.getCountryId(),
                request.getProvinceId(),
                request.getCityId(),
                request.getDistrictId()
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
        private Integer countryId;
        private Integer provinceId;
        private Integer cityId;
        private Integer districtId;
    }
}
