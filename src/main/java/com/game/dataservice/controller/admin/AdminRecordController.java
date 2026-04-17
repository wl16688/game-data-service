package com.game.dataservice.controller.admin;

import com.game.dataservice.common.ApiResponse;
import com.game.dataservice.entity.GameRecord;
import com.game.dataservice.repository.GameRecordRepository;
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
@RequestMapping("/api/admin/records")
@RequiredArgsConstructor
@Tag(name = "后台通关记录管理", description = "后台管理系统：查看和删除通关流水（需要 ADMIN JWT）")
@SecurityRequirement(name = "bearerAuth")
public class AdminRecordController {

    private final GameRecordRepository gameRecordRepository;

    @Operation(summary = "分页获取通关流水")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GameRecord>>> getRecords(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<GameRecord> recordPage;
        
        if (userId != null && !userId.trim().isEmpty()) {
            recordPage = gameRecordRepository.findByUserId(userId, pageable);
        } else {
            recordPage = gameRecordRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(recordPage));
    }

    @Operation(summary = "删除通关记录")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRecord(@PathVariable Long id) {
        if (!gameRecordRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "通关记录不存在"));
        }
        // 注意：删除流水不一定会自动同步减少 Redis 榜单中的分数。如果需要，应在此处调用 LeaderboardService 对该用户的分数做递减操作
        gameRecordRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("记录删除成功", null));
    }
}