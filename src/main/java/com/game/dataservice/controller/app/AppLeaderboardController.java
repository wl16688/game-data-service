package com.game.dataservice.controller.app;

import com.game.dataservice.common.ApiResponse;
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
@Tag(name = "小程序排行榜", description = "小程序端排行榜接口（需要 USER/ADMIN JWT）")
@SecurityRequirement(name = "bearerAuth")
public class AppLeaderboardController {

    private final LeaderboardService leaderboardService;
    private final LeaderboardCacheJob leaderboardCacheJob;

    @Operation(summary = "获取全服排行榜（缓存）", description = "获取全服前 N 名（day/week/month/all），每 5 分钟更新一次缓存")
    @GetMapping("/{gameId}/global")
    public ResponseEntity<?> getGlobalTop(
            @PathVariable Long gameId,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "100") int limit) {
        
        // 优先从 5 分钟缓存读取
        String cachedJson = leaderboardCacheJob.getCachedGlobalLeaderboard(gameId, period);
        if (cachedJson != null) {
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(cachedJson);
        }
        
        // 未命中缓存则直接查询 Redis ZSet 实时数据
        return ResponseEntity.ok(ApiResponse.success(leaderboardService.getGlobalLeaderboard(gameId, period, limit)));
    }

    @Operation(summary = "获取省内排行榜（缓存）", description = "获取某省前 N 名（day/week/month/all），每 5 分钟更新一次缓存")
    @GetMapping("/{gameId}/province/{provinceId}")
    public ResponseEntity<?> getProvinceTop(
            @PathVariable Long gameId,
            @PathVariable Integer provinceId,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "100") int limit) {
            
        // 优先从 5 分钟缓存读取
        String cachedJson = leaderboardCacheJob.getCachedProvinceLeaderboard(gameId, period, String.valueOf(provinceId));
        if (cachedJson != null) {
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(cachedJson);
        }
        
        // 未命中缓存则直接查询 Redis ZSet 实时数据
        return ResponseEntity.ok(ApiResponse.success(leaderboardService.getProvinceLeaderboard(gameId, period, provinceId, limit)));
    }

    @Operation(summary = "获取市内排行榜（实时）", description = "获取某市前 N 名（day/week/month/all），不做全量预热缓存")
    @GetMapping("/{gameId}/city/{cityId}")
    public ResponseEntity<ApiResponse<List<LeaderboardEntry>>> getCityTop(
            @PathVariable Long gameId,
            @PathVariable Integer cityId,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "100") int limit) {
        // 城市数量较多，不适合全量预热，直接查询实时 ZSet
        return ResponseEntity.ok(ApiResponse.success(leaderboardService.getCityLeaderboard(gameId, period, cityId, limit)));
    }

    @Operation(summary = "获取省份总榜（缓存）", description = "按通关总数对全国所有省份进行排行（day/week/month/all），每 5 分钟更新一次缓存")
    @GetMapping("/{gameId}/ranking/province")
    public ResponseEntity<?> getProvinceRanking(
            @PathVariable Long gameId,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "100") int limit) {
            
        String cachedJson = leaderboardCacheJob.getCachedRegionRanking(gameId, period, "prov");
        if (cachedJson != null) {
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(cachedJson);
        }
        
        return ResponseEntity.ok(ApiResponse.success(leaderboardService.getProvinceRanking(gameId, period, limit)));
    }

    @Operation(summary = "获取城市总榜（缓存）", description = "按通关总数对全国所有城市进行排行（day/week/month/all），每 5 分钟更新一次缓存")
    @GetMapping("/{gameId}/ranking/city")
    public ResponseEntity<?> getCityRanking(
            @PathVariable Long gameId,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "100") int limit) {
            
        String cachedJson = leaderboardCacheJob.getCachedRegionRanking(gameId, period, "city");
        if (cachedJson != null) {
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(cachedJson);
        }
        
        return ResponseEntity.ok(ApiResponse.success(leaderboardService.getCityRanking(gameId, period, limit)));
    }

    @Operation(summary = "获取我的统计信息", description = "获取当前用户的全服/省内/市内排名，以及当日通关数等统计")
    @GetMapping("/{gameId}/me/stats")
    public ResponseEntity<ApiResponse<UserStats>> getMyStats(
            @PathVariable Long gameId,
            @RequestParam(required = false) Integer provinceId,
            @RequestParam(required = false) Integer cityId) {
            
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) auth.getPrincipal();
        
        UserStats stats = leaderboardService.getUserStats(gameId, userId, provinceId, cityId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
