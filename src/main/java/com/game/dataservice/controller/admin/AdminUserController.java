package com.game.dataservice.controller.admin;

import com.game.dataservice.common.ApiResponse;
import com.game.dataservice.entity.User;
import com.game.dataservice.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "后台用户管理", description = "后台管理系统：玩家数据列表及操作（需要 ADMIN JWT）")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserRepository userRepository;

    @Operation(summary = "分页获取用户列表")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<User>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userRepository.findByNicknameContainingOrOpenidContaining(keyword, keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(userPage));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id, 
            @RequestBody User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setNickname(updatedUser.getNickname());
                    user.setAvatarUrl(updatedUser.getAvatarUrl());
                    user.setCountryId(updatedUser.getCountryId());
                    user.setProvinceId(updatedUser.getProvinceId());
                    user.setCityId(updatedUser.getCityId());
                    user.setDistrictId(updatedUser.getDistrictId());
                    return ResponseEntity.ok(ApiResponse.success(userRepository.save(user)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "用户不存在")));
    }
}