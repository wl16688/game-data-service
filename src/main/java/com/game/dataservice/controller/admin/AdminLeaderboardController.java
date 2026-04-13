package com.game.dataservice.controller.admin;

import com.game.dataservice.model.GameData;
import com.game.dataservice.service.DataSyncService;
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
    private final ObjectMapper objectMapper;

    @Operation(summary = "Manual Sync Data", description = "Manually push game data to the sync service (simulates Kafka message)")
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

    @Operation(summary = "Update User Score", description = "Admin endpoint to directly update a user's score")
    @PutMapping("/{gameId}/user/{userId}")
    public ResponseEntity<String> updateUserScore(
            @PathVariable String gameId,
            @PathVariable String userId,
            @RequestParam double score) {
        leaderboardService.updateScore(gameId, userId, score);
        return ResponseEntity.ok("Score updated successfully");
    }
}
