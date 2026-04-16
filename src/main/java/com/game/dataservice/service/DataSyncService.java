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
            log.info("收到游戏数据同步事件：{}", gameData);
            
            // 1. 处理同步事件：更新 Redis 榜单
            leaderboardService.recordLevelClear(
                    gameData.getGameId(), 
                    gameData.getUserId(), 
                    gameData.getProvinceId(), 
                    gameData.getCityId()
            );
            
            // 2. 将通关记录持久化到 MySQL
            GameRecord record = GameRecord.builder()
                    .gameId(gameData.getGameId())
                    .userId(gameData.getUserId())
                    .levelId(gameData.getLevelId())
                    .timestamp(gameData.getTimestamp())
                    .build();
            gameRecordRepository.save(record);
            log.debug("通关记录已落库，记录ID：{}", record.getId());
            
        } catch (Exception e) {
            log.error("处理游戏数据同步事件失败：{}", message, e);
        }
    }
}
