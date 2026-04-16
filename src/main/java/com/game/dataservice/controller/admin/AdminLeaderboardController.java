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
@Tag(name = "Admin Leaderboard", description = "Backend Admin Endpoints (Requires ADMIN JWT)")
@SecurityRequirement(name = "bearerAuth")
public class AdminLeaderboardController {

    private final LeaderboardService leaderboardService;
    private final DataSyncService dataSyncService;
    private final DisasterRecoveryService disasterRecoveryService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Manual Sync Data", description = "Manually push a level clear event (simulates Kafka message)")
    @PostMapping("/sync")
    public ResponseEntity<String> manualSync(@RequestBody GameData gameData) {
        try {
            String message = objectMapper.writeValueAsString(gameData);
            dataSyncService.consumeGameData(message);
            return ResponseEntity.ok("Data synced successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Sync failed: " + e.getMessage());
        }
    }

    @Operation(summary = "Force Update Score", description = "Force set absolute score for a specific leaderboard key (e.g. game:lb:global:1)")
    @PutMapping("/force-score")
    public ResponseEntity<String> forceUpdateScore(
            @RequestParam String key,
            @RequestParam String userId,
            @RequestParam double score) {
        leaderboardService.updateAbsoluteScore(key, userId, score);
        return ResponseEntity.ok("Score forced updated successfully");
    }

    @Operation(summary = "Trigger Disaster Recovery", description = "Rebuild all Redis leaderboards (day/week/month/all) from MySQL game records for a specific game.")
    @PostMapping("/disaster-recovery/{gameId}")
    public ResponseEntity<String> triggerDisasterRecovery(@PathVariable String gameId) {
        try {
            // 在生产环境中，为了防止超时，这里可以使用异步线程池去跑，或者丢给MQ去跑。
            // 这里为了演示直接同步调用
            disasterRecoveryService.recoverRedisLeaderboards(gameId);
            return ResponseEntity.ok("Disaster recovery completed successfully for game: " + gameId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Disaster recovery failed: " + e.getMessage());
        }
    }
}
