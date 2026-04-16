package com.game.dataservice.controller.admin;

import com.game.dataservice.model.GameData;
import com.game.dataservice.service.DataSyncService;
import com.game.dataservice.service.DisasterRecoveryService;
import com.game.dataservice.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/admin/leaderboard")
@RequiredArgsConstructor
@Tag(name = "管理端排行榜", description = "管理端排行榜与数据同步接口（需要 ADMIN JWT）")
@SecurityRequirement(name = "bearerAuth")
public class AdminLeaderboardController {

    private final LeaderboardService leaderboardService;
    private final DataSyncService dataSyncService;
    private final DisasterRecoveryService disasterRecoveryService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "手动同步通关数据", description = "手动提交一次通关事件（用于模拟 Kafka 消息）")
    @PostMapping("/sync")
    public ResponseEntity<String> manualSync(@RequestBody GameData gameData) {
        try {
            String message = objectMapper.writeValueAsString(gameData);
            dataSyncService.consumeGameData(message);
            return ResponseEntity.ok("同步成功");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("同步失败：" + e.getMessage());
        }
    }

    @Operation(summary = "强制设置榜单分数", description = "为指定榜单 Key 强制写入绝对分数（例如：game:lb:global:xxx）")
    @PutMapping("/force-score")
    public ResponseEntity<String> forceUpdateScore(
            @RequestParam String key,
            @RequestParam String userId,
            @RequestParam double score) {
        leaderboardService.updateAbsoluteScore(key, userId, score);
        return ResponseEntity.ok("写入成功");
    }

    @Operation(summary = "触发容灾恢复", description = "基于 MySQL 通关流水重建 Redis 榜单（day/week/month/all）")
    @PostMapping("/disaster-recovery/{gameId}")
    public ResponseEntity<String> triggerDisasterRecovery(@PathVariable String gameId) {
        try {
            // 在生产环境中，为了防止超时，这里可以使用异步线程池去跑，或者丢给MQ去跑。
            // 这里为了演示直接同步调用
            disasterRecoveryService.recoverRedisLeaderboards(gameId);
            return ResponseEntity.ok("容灾恢复完成，游戏：" + gameId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("容灾恢复失败：" + e.getMessage());
        }
    }
}
