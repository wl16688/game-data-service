package com.game.dataservice.service;

import com.game.dataservice.model.GameData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncService {

    private final LeaderboardService leaderboardService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${game.sync.topic:game-data-sync}", groupId = "${spring.kafka.consumer.group-id:game-service-group}")
    public void consumeGameData(String message) {
        try {
            GameData gameData = objectMapper.readValue(message, GameData.class);
            log.info("Received game data sync event: {}", gameData);
            
            // Process the sync event, e.g., update leaderboard
            leaderboardService.updateScore(gameData.getGameId(), gameData.getUserId(), gameData.getScore());
            
            // Further asynchronous tasks like saving to DB could go here
            // e.g. gameDataRepository.save(gameData);
            
        } catch (Exception e) {
            log.error("Failed to process game data sync event: {}", message, e);
        }
    }
}
