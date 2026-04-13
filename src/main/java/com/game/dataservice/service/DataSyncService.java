package com.game.dataservice.service;

import com.game.dataservice.entity.GameRecord;
import com.game.dataservice.model.GameData;
import com.game.dataservice.repository.GameRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {

    private final LeaderboardService leaderboardService;
    private final GameRecordRepository gameRecordRepository;
    private final ObjectMapper objectMapper;

    // @KafkaListener(topics = "${game.sync.topic:game-data-sync}", groupId = "${spring.kafka.consumer.group-id:game-service-group}")
    public void consumeGameData(String message) {
        try {
            GameData gameData = objectMapper.readValue(message, GameData.class);
            log.info("Received game data sync event: {}", gameData);
            
            // 1. Process the sync event, e.g., update leaderboard in Redis
            leaderboardService.updateScore(gameData.getGameId(), gameData.getUserId(), gameData.getScore());
            
            // 2. Persist the game data record into MySQL
            GameRecord record = GameRecord.builder()
                    .gameId(gameData.getGameId())
                    .userId(gameData.getUserId())
                    .score(gameData.getScore())
                    .timestamp(gameData.getTimestamp())
                    .build();
            gameRecordRepository.save(record);
            log.debug("Persisted game record to database: {}", record.getId());
            
        } catch (Exception e) {
            log.error("Failed to process game data sync event: {}", message, e);
        }
    }
}
