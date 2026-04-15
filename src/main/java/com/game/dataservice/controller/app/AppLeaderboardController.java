package com.game.dataservice.controller.app;

import com.game.dataservice.model.LeaderboardEntry;
import com.game.dataservice.model.UserStats;
import com.game.dataservice.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/app/leaderboard")
@RequiredArgsConstructor
@Tag(name = "App Leaderboard", description = "Frontend Client Endpoints (Requires USER/ADMIN JWT)")
@SecurityRequirement(name = "bearerAuth")
public class AppLeaderboardController {

    private final LeaderboardService leaderboardService;

    @Operation(summary = "Get Global Top Players", description = "Fetch the top N players globally")
    @GetMapping("/{gameId}/global")
    public ResponseEntity<List<LeaderboardEntry>> getGlobalTop(
            @PathVariable String gameId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(leaderboardService.getGlobalLeaderboard(gameId, limit));
    }

    @Operation(summary = "Get Province Top Players", description = "Fetch the top N players in a specific province")
    @GetMapping("/{gameId}/province/{province}")
    public ResponseEntity<List<LeaderboardEntry>> getProvinceTop(
            @PathVariable String gameId,
            @PathVariable String province,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(leaderboardService.getProvinceLeaderboard(gameId, province, limit));
    }

    @Operation(summary = "Get City Top Players", description = "Fetch the top N players in a specific city")
    @GetMapping("/{gameId}/city/{city}")
    public ResponseEntity<List<LeaderboardEntry>> getCityTop(
            @PathVariable String gameId,
            @PathVariable String city,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(leaderboardService.getCityLeaderboard(gameId, city, limit));
    }

    @Operation(summary = "Get My Stats", description = "Fetch stats (global/prov/city ranks, daily levels) for the current user")
    @GetMapping("/{gameId}/me/stats")
    public ResponseEntity<UserStats> getMyStats(
            @PathVariable String gameId,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city) {
            
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        
        UserStats stats = leaderboardService.getUserStats(gameId, userId, province, city);
        return ResponseEntity.ok(stats);
    }
}
