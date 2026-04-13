package com.game.dataservice.controller.app;

import com.game.dataservice.model.LeaderboardEntry;
import com.game.dataservice.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/app/leaderboard")
@RequiredArgsConstructor
@Tag(name = "App Leaderboard", description = "Frontend Client Endpoints (Requires USER/ADMIN JWT)")
@SecurityRequirement(name = "bearerAuth")
public class AppLeaderboardController {

    private final LeaderboardService leaderboardService;

    @Operation(summary = "Get Top Players", description = "Fetch the top N players for a specific game")
    @GetMapping("/{gameId}/top")
    public ResponseEntity<List<LeaderboardEntry>> getTopPlayers(
            @PathVariable String gameId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(leaderboardService.getTopPlayers(gameId, limit));
    }

    @Operation(summary = "Get User Rank", description = "Fetch rank and score for a specific user")
    @GetMapping("/{gameId}/user/{userId}")
    public ResponseEntity<LeaderboardEntry> getUserRank(
            @PathVariable String gameId,
            @PathVariable String userId) {
        LeaderboardEntry entry = leaderboardService.getUserRank(gameId, userId);
        if (entry == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entry);
    }
}
