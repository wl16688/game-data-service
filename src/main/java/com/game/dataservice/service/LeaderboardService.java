package com.game.dataservice.service;

import com.game.dataservice.model.LeaderboardEntry;
import com.game.dataservice.model.RegionLeaderboardEntry;
import com.game.dataservice.model.UserStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RegionCacheService regionCacheService;

    // Keys base definition
    private static final String KEY_GLOBAL = "game:lb:global:";
    private static final String KEY_PROV = "game:lb:prov:";
    private static final String KEY_CITY = "game:lb:city:";
    private static final String KEY_REGION_PROV = "game:lb:region:prov:";
    private static final String KEY_REGION_CITY = "game:lb:region:city:";
    private static final String KEY_DAILY = "game:daily:clears:";
    
    // Period specific formats
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 记录玩家通关事件 (按周期更新)
     */
    public void recordLevelClear(String gameId, String userId, Integer provinceId, Integer cityId) {
        LocalDate now = LocalDate.now();
        String dayStr = now.format(DAY_FORMATTER);
        String weekStr = now.getYear() + "W" + now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        String monthStr = now.format(MONTH_FORMATTER);

        // 1. 更新总榜单
        incrementScores(gameId, userId, provinceId, cityId, "all", 0);
        
        // 2. 更新日榜单 (保留3天)
        incrementScores(gameId + ":day:" + dayStr, userId, provinceId, cityId, "day", 3);
        
        // 3. 更新周榜单 (保留14天)
        incrementScores(gameId + ":week:" + weekStr, userId, provinceId, cityId, "week", 14);
        
        // 4. 更新月榜单 (保留60天)
        incrementScores(gameId + ":month:" + monthStr, userId, provinceId, cityId, "month", 60);

        // 5. 更新独立的每日通关统计Hash (用于用户个人中心展示)
        String dailyKey = KEY_DAILY + gameId + ":" + dayStr;
        redisTemplate.opsForHash().increment(dailyKey, userId, 1);
        redisTemplate.expire(dailyKey, Duration.ofDays(2));

        log.debug("Recorded level clear for user {} in game {}, provId: {}, cityId: {}", userId, gameId, provinceId, cityId);
    }

    private void incrementScores(String gamePeriodKey, String userId, Integer provinceId, Integer cityId, String type, int expireDays) {
        String globalKey = KEY_GLOBAL + gamePeriodKey;
        redisTemplate.opsForZSet().incrementScore(globalKey, userId, 1);
        if (expireDays > 0) redisTemplate.expire(globalKey, Duration.ofDays(expireDays));

        if (provinceId != null) {
            String provKey = KEY_PROV + gamePeriodKey + ":" + provinceId;
            redisTemplate.opsForZSet().incrementScore(provKey, userId, 1);
            if (expireDays > 0) redisTemplate.expire(provKey, Duration.ofDays(expireDays));
            
            // 同时更新“省份通关总数排行榜”
            String regionProvKey = KEY_REGION_PROV + gamePeriodKey;
            redisTemplate.opsForZSet().incrementScore(regionProvKey, String.valueOf(provinceId), 1);
            if (expireDays > 0) redisTemplate.expire(regionProvKey, Duration.ofDays(expireDays));
        }

        if (cityId != null) {
            String cityKey = KEY_CITY + gamePeriodKey + ":" + cityId;
            redisTemplate.opsForZSet().incrementScore(cityKey, userId, 1);
            if (expireDays > 0) redisTemplate.expire(cityKey, Duration.ofDays(expireDays));
            
            // 同时更新“城市通关总数排行榜”
            String regionCityKey = KEY_REGION_CITY + gamePeriodKey;
            redisTemplate.opsForZSet().incrementScore(regionCityKey, String.valueOf(cityId), 1);
            if (expireDays > 0) redisTemplate.expire(regionCityKey, Duration.ofDays(expireDays));
        }
    }

    /**
     * 通用的拉取排行榜方法 (从缓存的 ZSet 中取)
     */
    public List<LeaderboardEntry> getLeaderboard(String key, int topN) {
        Set<ZSetOperations.TypedTuple<Object>> topScores = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, topN - 1);

        if (topScores == null || topScores.isEmpty()) {
            return Collections.emptyList();
        }

        long rank = 1;
        List<LeaderboardEntry> leaderboard = new java.util.ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : topScores) {
            leaderboard.add(LeaderboardEntry.builder()
                    .userId(String.valueOf(tuple.getValue()))
                    .score(tuple.getScore() != null ? tuple.getScore() : 0)
                    .rank(rank++)
                    .build());
        }
        return leaderboard;
    }

    /**
     * 强制更新某个排行榜的绝对分数 (供管理员调用)
     */
    public void updateAbsoluteScore(String key, String userId, double score) {
        redisTemplate.opsForZSet().add(key, userId, score);
    }

    /**
     * 辅助方法生成周期 Key
     */
    public String getPeriodKey(String gameId, String period) {
        LocalDate now = LocalDate.now();
        return switch (period.toLowerCase()) {
            case "day" -> gameId + ":day:" + now.format(DAY_FORMATTER);
            case "week" -> gameId + ":week:" + now.getYear() + "W" + now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            case "month" -> gameId + ":month:" + now.format(MONTH_FORMATTER);
            case "all" -> gameId;
            default -> gameId;
        };
    }

    /**
     * 获取全局榜单
     */
    public List<LeaderboardEntry> getGlobalLeaderboard(String gameId, String period, int topN) {
        return getLeaderboard(KEY_GLOBAL + getPeriodKey(gameId, period), topN);
    }

    /**
     * 获取省份榜单
     */
    public List<LeaderboardEntry> getProvinceLeaderboard(String gameId, String period, Integer provinceId, int topN) {
        return getLeaderboard(KEY_PROV + getPeriodKey(gameId, period) + ":" + provinceId, topN);
    }

    /**
     * 获取城市榜单
     */
    public List<LeaderboardEntry> getCityLeaderboard(String gameId, String period, Integer cityId, int topN) {
        return getLeaderboard(KEY_CITY + getPeriodKey(gameId, period) + ":" + cityId, topN);
    }

    /**
     * 通用的拉取地区总分排行榜方法
     */
    public List<RegionLeaderboardEntry> getRegionLeaderboard(String key, int topN) {
        Set<ZSetOperations.TypedTuple<Object>> topScores = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, topN - 1);

        if (topScores == null || topScores.isEmpty()) {
            return Collections.emptyList();
        }

        long rank = 1;
        List<RegionLeaderboardEntry> leaderboard = new java.util.ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : topScores) {
            Integer regionId = Integer.valueOf(String.valueOf(tuple.getValue()));
            leaderboard.add(RegionLeaderboardEntry.builder()
                    .regionId(regionId)
                    .regionName(regionCacheService.getName(regionId))
                    .totalLevels(tuple.getScore() != null ? tuple.getScore() : 0)
                    .rank(rank++)
                    .build());
        }
        return leaderboard;
    }

    /**
     * 获取省份之间的排行榜
     */
    public List<RegionLeaderboardEntry> getProvinceRanking(String gameId, String period, int topN) {
        return getRegionLeaderboard(KEY_REGION_PROV + getPeriodKey(gameId, period), topN);
    }

    /**
     * 获取城市之间的排行榜
     */
    public List<RegionLeaderboardEntry> getCityRanking(String gameId, String period, int topN) {
        return getRegionLeaderboard(KEY_REGION_CITY + getPeriodKey(gameId, period), topN);
    }

    /**
     * 获取特定用户的多维度统计数据
     */
    public UserStats getUserStats(String gameId, String userId, Integer provinceId, Integer cityId) {
        String allPeriodKey = getPeriodKey(gameId, "all");
        
        Double globalScore = redisTemplate.opsForZSet().score(KEY_GLOBAL + allPeriodKey, userId);
        Long globalRank = redisTemplate.opsForZSet().reverseRank(KEY_GLOBAL + allPeriodKey, userId);

        Long provRank = null;
        if (provinceId != null) {
            provRank = redisTemplate.opsForZSet().reverseRank(KEY_PROV + allPeriodKey + ":" + provinceId, userId);
        }

        Long cityRank = null;
        if (cityId != null) {
            cityRank = redisTemplate.opsForZSet().reverseRank(KEY_CITY + allPeriodKey + ":" + cityId, userId);
        }

        String today = LocalDate.now().format(DAY_FORMATTER);
        String dailyKey = KEY_DAILY + gameId + ":" + today;
        Object dailyObj = redisTemplate.opsForHash().get(dailyKey, userId);
        Long dailyLevels = dailyObj != null ? Long.parseLong(dailyObj.toString()) : 0L;

        return UserStats.builder()
                .userId(userId)
                .gameId(gameId)
                .totalLevels(globalScore != null ? globalScore : 0.0)
                .dailyLevels(dailyLevels)
                .globalRank(globalRank != null ? globalRank + 1 : null)
                .provinceRank(provRank != null ? provRank + 1 : null)
                .cityRank(cityRank != null ? cityRank + 1 : null)
                .build();
    }
}
