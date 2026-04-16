package com.game.dataservice.repository;

import com.game.dataservice.entity.GamePlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GamePlatformRepository extends JpaRepository<GamePlatform, Long> {
    List<GamePlatform> findByGameId(Long gameId);
}

