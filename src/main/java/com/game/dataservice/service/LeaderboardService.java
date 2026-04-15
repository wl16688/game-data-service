package com.game.dataservice.service;

import com.game.dataservice.model.LeaderboardEntry;
import com.game.dataservice.model.UserStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String KEY_GLOBAL = "game:lb:global:";
    private static final String KEY_PROV = "game:lb:prov:";
    private static final String KEY_CITY = "game:lb:city:";
    private static final String KEY_DAILY = "game:daily:clears:";

    /**
     * 记录玩家通关事件
     */
    public void recordLevelClear(String gameId, String userId, String province, String city) {
        // 1. 更新全球排行榜 (累加关卡数)
        redisTemplate.opsForZSet().incrementScore(KEY_GLOBAL + gameId, userId, 1);
        
        // 2. 更新省份排行榜
        if (province != null && !province.isEmpty()) {
            redisTemplate.opsForZSet().incrementScore(KEY_PROV + gameId + ":" + province, userId, 1);
        }
        
        // 3. 更新城市排行榜
        if (city != null && !city.isEmpty()) {
            redisTemplate.opsForZSet().incrementScore(KEY_CITY + gameId + ":" + city, userId, 1);
        }
        
        // 4. 更新每日通关统计 (有效期2天)
        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String dailyKey = KEY_DAILY + gameId + ":" + today;
        redisTemplate.opsForHash().increment(dailyKey, userId, 1);
        redisTemplate.expire(dailyKey, Duration.ofDays(2));
        
        log.debug("Recorded level clear for user {} in game {}, prov: {}, city: {}", userId, gameId, province, city);
    }

    /**
     * 强制更新某个排行榜的绝对分数 (供管理员调用)
     */
    public void updateAbsoluteScore(String key, String userId, double score) {
        redisTemplate.opsForZSet().add(key, userId, score);
    }

    /**
     * 通用的拉取排行榜方法
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
     * 获取全局榜单
     */
    public List<LeaderboardEntry> getGlobalLeaderboard(String gameId, int topN) {
        return getLeaderboard(KEY_GLOBAL + gameId, topN);
    }

    /**
     * 获取省份榜单
     */
    public List<LeaderboardEntry> getProvinceLeaderboard(String gameId, String province, int topN) {
        return getLeaderboard(KEY_PROV + gameId + ":" + province, topN);
    }

    /**
     * 获取城市榜单
     */
    public List<LeaderboardEntry> getCityLeaderboard(String gameId, String city, int topN) {
        return getLeaderboard(KEY_CITY + gameId + ":" + city, topN);
    }

    /**
     * 获取特定用户的多维度统计数据
     */
    public UserStats getUserStats(String gameId, String userId, String province, String city) {
        Double globalScore = redisTemplate.opsForZSet().score(KEY_GLOBAL + gameId, userId);
        Long globalRank = redisTemplate.opsForZSet().reverseRank(KEY_GLOBAL + gameId, userId);
        
        Long provRank = null;
        if (province != null && !province.isEmpty()) {
            provRank = redisTemplate.opsForZSet().reverseRank(KEY_PROV + gameId + ":" + province, userId);
        }
        
        Long cityRank = null;
        if (city != null && !city.isEmpty()) {
            cityRank = redisTemplate.opsForZSet().reverseRank(KEY_CITY + gameId + ":" + city, userId);
        }

        String today = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
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
    
    @Deprecated // Keep backward compatibility briefly if needed
    public LeaderboardEntry getUserRank(String gameId, String userId) {
        Double score = redisTemplate.opsForZSet().score(KEY_GLOBAL + gameId, userId);
        Long rank = redisTemplate.opsForZSet().reverseRank(KEY_GLOBAL + gameId, userId);
        if (rank == null || score == null) return null;
        return LeaderboardEntry.builder().userId(userId).score(score).rank(rank + 1).build();
    }
}
