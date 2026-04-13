package com.game.dataservice.repository;

import com.game.dataservice.entity.GameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {
    List<GameRecord> findByGameIdAndUserId(String gameId, String userId);
}
