package com.game.dataservice.controller;

import com.game.dataservice.model.LeaderboardEntry;
import com.game.dataservice.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/{gameId}/top")
    public ResponseEntity<List<LeaderboardEntry>> getTopPlayers(
            @PathVariable String gameId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(leaderboardService.getTopPlayers(gameId, limit));
    }

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
