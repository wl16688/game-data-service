package com.game.dataservice.controller.app;

import com.game.dataservice.model.LeaderboardEntry;
import com.game.dataservice.model.UserStats;
import com.game.dataservice.service.LeaderboardCacheJob;
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
    private final LeaderboardCacheJob leaderboardCacheJob;

    @Operation(summary = "Get Global Top Players (Cached)", description = "Fetch top N players globally (day, week, month, all). Updated every 5 mins.")
    @GetMapping("/{gameId}/global")
    public ResponseEntity<?> getGlobalTop(
            @PathVariable String gameId,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "100") int limit) {
        
        // 尝试从5分钟缓存读取
        String cachedJson = leaderboardCacheJob.getCachedGlobalLeaderboard(gameId, period);
        if (cachedJson != null) {
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(cachedJson);
        }
        
        // 兜底直接查Redis ZSet实时数据
        return ResponseEntity.ok(leaderboardService.getGlobalLeaderboard(gameId, period, limit));
    }

    @Operation(summary = "Get Province Top Players (Cached)", description = "Fetch top N players in a province (day, week, month, all). Updated every 5 mins.")
    @GetMapping("/{gameId}/province/{province}")
    public ResponseEntity<?> getProvinceTop(
            @PathVariable String gameId,
            @PathVariable String province,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "100") int limit) {
            
        // 尝试从5分钟缓存读取
        String cachedJson = leaderboardCacheJob.getCachedProvinceLeaderboard(gameId, period, province);
        if (cachedJson != null) {
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(cachedJson);
        }
        
        // 兜底直接查Redis ZSet实时数据
        return ResponseEntity.ok(leaderboardService.getProvinceLeaderboard(gameId, period, province, limit));
    }

    @Operation(summary = "Get City Top Players (Real-time)", description = "Fetch top N players in a specific city")
    @GetMapping("/{gameId}/city/{city}")
    public ResponseEntity<List<LeaderboardEntry>> getCityTop(
            @PathVariable String gameId,
            @PathVariable String city,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "100") int limit) {
        // City 太多不适合全部预热，采用直接查实时ZSet的方式
        return ResponseEntity.ok(leaderboardService.getCityLeaderboard(gameId, period, city, limit));
    }

    @Operation(summary = "Get Provinces Ranking (Cached)", description = "Fetch ranking of all provinces by total level clears")
    @GetMapping("/{gameId}/ranking/province")
    public ResponseEntity<?> getProvinceRanking(
            @PathVariable String gameId,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "100") int limit) {
            
        String cachedJson = leaderboardCacheJob.getCachedRegionRanking(gameId, period, "prov");
        if (cachedJson != null) {
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(cachedJson);
        }
        
        return ResponseEntity.ok(leaderboardService.getProvinceRanking(gameId, period, limit));
    }

    @Operation(summary = "Get Cities Ranking (Cached)", description = "Fetch ranking of all cities by total level clears")
    @GetMapping("/{gameId}/ranking/city")
    public ResponseEntity<?> getCityRanking(
            @PathVariable String gameId,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "100") int limit) {
            
        String cachedJson = leaderboardCacheJob.getCachedRegionRanking(gameId, period, "city");
        if (cachedJson != null) {
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(cachedJson);
        }
        
        return ResponseEntity.ok(leaderboardService.getCityRanking(gameId, period, limit));
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
