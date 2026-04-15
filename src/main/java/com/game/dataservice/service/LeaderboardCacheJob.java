package com.game.dataservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.dataservice.model.LeaderboardEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardCacheJob {

    private final LeaderboardService leaderboardService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 为了演示，这里固定了需要计算缓存的 gameId 和地区。实际业务中可以从 DB/配置中动态获取。
    private final List<String> activeGames = Arrays.asList("sheep_game");
    private final List<String> periods = Arrays.asList("day", "week", "month", "all");
    private final List<String> topProvinces = Arrays.asList("Guangdong", "Beijing", "Shanghai"); // 热门省份预计算
    private final int TOP_N = 100;

    private static final String CACHE_PREFIX = "game:lb:cache:";

    /**
     * 每 5 分钟执行一次 (5 * 60 * 1000 = 300000ms)
     * 将实时 ZSet 的排行榜数据转为 JSON 字符串存入 String 类型并设置 5 分钟有效期。
     * 这样前端的并发查询直接走 String 缓存，不用每次查 ZSet。
     */
    @Scheduled(fixedRate = 300000)
    public void refreshLeaderboardCache() {
        log.info("Starting leaderboard cache refresh job...");
        try {
            for (String gameId : activeGames) {
                for (String period : periods) {
                    // 1. 缓存全球榜单
                    cacheGlobalLeaderboard(gameId, period);
                    
                    // 2. 缓存地区排行总榜（各省份总分、各城市总分排行）
                    cacheRegionRanking(gameId, period);
                    
                    // 3. 缓存具体热门省份内的玩家排行
                    for (String province : topProvinces) {
                        cacheProvinceLeaderboard(gameId, period, province);
                    }
                }
            }
            log.info("Finished leaderboard cache refresh job.");
        } catch (Exception e) {
            log.error("Error refreshing leaderboard cache", e);
        }
    }

    private void cacheGlobalLeaderboard(String gameId, String period) throws Exception {
        List<LeaderboardEntry> list = leaderboardService.getGlobalLeaderboard(gameId, period, TOP_N);
        String json = objectMapper.writeValueAsString(list);
        String cacheKey = CACHE_PREFIX + "global:" + gameId + ":" + period;
        redisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(6)); // 稍微多给一分钟，防止并发时穿透
        log.debug("Cached global leaderboard for game {}, period {}", gameId, period);
    }

    private void cacheProvinceLeaderboard(String gameId, String period, String province) throws Exception {
        List<LeaderboardEntry> list = leaderboardService.getProvinceLeaderboard(gameId, period, province, TOP_N);
        String json = objectMapper.writeValueAsString(list);
        String cacheKey = CACHE_PREFIX + "prov:" + gameId + ":" + period + ":" + province;
        redisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(6));
    }

    private void cacheRegionRanking(String gameId, String period) throws Exception {
        // 省份排行缓存
        List<com.game.dataservice.model.RegionLeaderboardEntry> provList = leaderboardService.getProvinceRanking(gameId, period, TOP_N);
        String provJson = objectMapper.writeValueAsString(provList);
        redisTemplate.opsForValue().set(CACHE_PREFIX + "ranking:prov:" + gameId + ":" + period, provJson, Duration.ofMinutes(6));

        // 城市排行缓存
        List<com.game.dataservice.model.RegionLeaderboardEntry> cityList = leaderboardService.getCityRanking(gameId, period, TOP_N);
        String cityJson = objectMapper.writeValueAsString(cityList);
        redisTemplate.opsForValue().set(CACHE_PREFIX + "ranking:city:" + gameId + ":" + period, cityJson, Duration.ofMinutes(6));
    }

    /**
     * 供 Controller 使用的查询缓存方法
     */
    public String getCachedGlobalLeaderboard(String gameId, String period) {
        String cacheKey = CACHE_PREFIX + "global:" + gameId + ":" + period;
        return (String) redisTemplate.opsForValue().get(cacheKey);
    }

    public String getCachedProvinceLeaderboard(String gameId, String period, String province) {
        String cacheKey = CACHE_PREFIX + "prov:" + gameId + ":" + period + ":" + province;
        return (String) redisTemplate.opsForValue().get(cacheKey);
    }

    public String getCachedRegionRanking(String gameId, String period, String regionType) {
        String cacheKey = CACHE_PREFIX + "ranking:" + regionType + ":" + gameId + ":" + period;
        return (String) redisTemplate.opsForValue().get(cacheKey);
    }
}
