package com.game.dataservice.service;

import com.game.dataservice.repository.GameRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisasterRecoveryService {

    private final GameRecordRepository gameRecordRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LeaderboardService leaderboardService;

    /**
     * 触发全量 Redis 容灾恢复（针对某个 GameId）
     * 该操作较重，生产环境建议异步执行或在业务低峰期执行
     */
    public void recoverRedisLeaderboards(String gameId) {
        log.warn("开始执行 Redis 容灾恢复，gameId：{}", gameId);
        
        LocalDate today = LocalDate.now();
        
        // 1. 恢复今日数据
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        String dayPeriodKey = leaderboardService.getPeriodKey(gameId, "day");
        recoverPeriodData(gameId, dayPeriodKey, startOfDay, endOfDay, 3);
        
        // 2. 恢复本周数据
        LocalDate firstDayOfWeek = today.with(WeekFields.ISO.dayOfWeek(), 1);
        LocalDateTime startOfWeek = firstDayOfWeek.atStartOfDay();
        LocalDateTime endOfWeek = firstDayOfWeek.plusWeeks(1).atStartOfDay();
        String weekPeriodKey = leaderboardService.getPeriodKey(gameId, "week");
        recoverPeriodData(gameId, weekPeriodKey, startOfWeek, endOfWeek, 14);

        // 3. 恢复本月数据
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();
        LocalDateTime endOfMonth = firstDayOfMonth.plusMonths(1).atStartOfDay();
        String monthPeriodKey = leaderboardService.getPeriodKey(gameId, "month");
        recoverPeriodData(gameId, monthPeriodKey, startOfMonth, endOfMonth, 60);

        // 4. 恢复总榜数据
        LocalDateTime minTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime maxTime = LocalDateTime.of(2100, 1, 1, 0, 0);
        String allPeriodKey = leaderboardService.getPeriodKey(gameId, "all");
        recoverPeriodData(gameId, allPeriodKey, minTime, maxTime, 0);

        log.warn("Redis 容灾恢复完成，gameId：{}", gameId);
    }

    /**
     * 恢复特定时间段的排行榜数据
     */
    private void recoverPeriodData(String gameId, String periodKey, LocalDateTime start, LocalDateTime end, int expireDays) {
        log.info("开始恢复周期榜单：{}（{} ~ {}）", periodKey, start, end);

        // 1. 恢复全服玩家榜
        List<Map<String, Object>> globalScores = gameRecordRepository.countUserScoresByPeriod(gameId, start, end);
        String globalKey = "game:lb:global:" + periodKey;
        writeToZSet(globalKey, globalScores, "userId", expireDays);

        // 2. 恢复全国各省份总分榜
        List<Map<String, Object>> provScores = gameRecordRepository.countProvinceScoresByPeriod(gameId, start, end);
        String regionProvKey = "game:lb:region:prov:" + periodKey;
        writeToZSet(regionProvKey, provScores, "regionId", expireDays);

        // 3. 恢复全国各城市总分榜
        List<Map<String, Object>> cityScores = gameRecordRepository.countCityScoresByPeriod(gameId, start, end);
        String regionCityKey = "game:lb:region:city:" + periodKey;
        writeToZSet(regionCityKey, cityScores, "regionId", expireDays);

        // 4. 恢复省份内部玩家榜（找出这段时间内有活跃的省份）
        Set<Integer> activeProvinces = new HashSet<>();
        provScores.forEach(m -> activeProvinces.add(((Number) m.get("regionId")).intValue()));
        for (Integer provId : activeProvinces) {
            List<Map<String, Object>> userProvScores = gameRecordRepository.countUserScoresByProvinceAndPeriod(gameId, provId, start, end);
            String provUserKey = "game:lb:prov:" + periodKey + ":" + provId;
            writeToZSet(provUserKey, userProvScores, "userId", expireDays);
        }

        // 5. 恢复城市内部玩家榜（找出这段时间内有活跃的城市）
        Set<Integer> activeCities = new HashSet<>();
        cityScores.forEach(m -> activeCities.add(((Number) m.get("regionId")).intValue()));
        for (Integer cityId : activeCities) {
            List<Map<String, Object>> userCityScores = gameRecordRepository.countUserScoresByCityAndPeriod(gameId, cityId, start, end);
            String cityUserKey = "game:lb:city:" + periodKey + ":" + cityId;
            writeToZSet(cityUserKey, userCityScores, "userId", expireDays);
        }
    }

    /**
     * 批量写入 Redis ZSet
     */
    private void writeToZSet(String key, List<Map<String, Object>> data, String memberField, int expireDays) {
        if (data == null || data.isEmpty()) return;

        redisTemplate.delete(key); // 清除旧脏数据

        Set<ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>();
        for (Map<String, Object> row : data) {
            String member = String.valueOf(row.get(memberField));
            Double score = Double.valueOf(row.get("score").toString());
            tuples.add(ZSetOperations.TypedTuple.of(member, score));
        }

        redisTemplate.opsForZSet().add(key, tuples);
        if (expireDays > 0) {
            redisTemplate.expire(key, Duration.ofDays(expireDays));
        }
    }
}
