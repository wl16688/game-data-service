package com.game.dataservice.service;

import com.game.dataservice.model.LeaderboardEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String LEADERBOARD_KEY_PREFIX = "game:leaderboard:";

    /**
     * Updates the user's score on the leaderboard.
     * Uses ZINCRBY if we want cumulative scores, or ZADD if we want to replace or take the max score.
     * Assuming here we just replace the score or add it if it's new.
     */
    public void updateScore(String gameId, String userId, double score) {
        String key = LEADERBOARD_KEY_PREFIX + gameId;
        redisTemplate.opsForZSet().add(key, userId, score);
        log.debug("Updated score for user {} in game {}: {}", userId, gameId, score);
    }

    /**
     * Gets the top N players from the leaderboard.
     */
    public List<LeaderboardEntry> getTopPlayers(String gameId, int topN) {
        String key = LEADERBOARD_KEY_PREFIX + gameId;
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
     * Gets the rank and score for a specific user.
     */
    public LeaderboardEntry getUserRank(String gameId, String userId) {
        String key = LEADERBOARD_KEY_PREFIX + gameId;
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userId);
        Double score = redisTemplate.opsForZSet().score(key, userId);
        
        if (rank == null || score == null) {
            return null;
        }
        
        return LeaderboardEntry.builder()
                .userId(userId)
                .score(score)
                .rank(rank + 1) // Redis rank is 0-based
                .build();
    }
}
