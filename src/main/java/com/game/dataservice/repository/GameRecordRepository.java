package com.game.dataservice.repository;

import com.game.dataservice.entity.GameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {
    List<GameRecord> findByGameIdAndUserId(String gameId, String userId);

    /**
     * 容灾恢复：统计指定时间段内，各用户的通关总数
     */
    @Query("SELECT r.userId as userId, COUNT(r) as score " +
           "FROM GameRecord r " +
           "WHERE r.gameId = :gameId " +
           "AND r.createdAt >= :startTime AND r.createdAt < :endTime " +
           "GROUP BY r.userId")
    List<Map<String, Object>> countUserScoresByPeriod(
            @Param("gameId") String gameId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 容灾恢复：统计指定时间段内，某省份内各用户的通关总数
     */
    @Query("SELECT r.userId as userId, COUNT(r) as score " +
           "FROM GameRecord r JOIN User u ON CAST(r.userId AS long) = u.id " +
           "WHERE r.gameId = :gameId AND u.province = :province " +
           "AND r.createdAt >= :startTime AND r.createdAt < :endTime " +
           "GROUP BY r.userId")
    List<Map<String, Object>> countUserScoresByProvinceAndPeriod(
            @Param("gameId") String gameId,
            @Param("province") String province,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 容灾恢复：统计指定时间段内，某城市内各用户的通关总数
     */
    @Query("SELECT r.userId as userId, COUNT(r) as score " +
           "FROM GameRecord r JOIN User u ON CAST(r.userId AS long) = u.id " +
           "WHERE r.gameId = :gameId AND u.city = :city " +
           "AND r.createdAt >= :startTime AND r.createdAt < :endTime " +
           "GROUP BY r.userId")
    List<Map<String, Object>> countUserScoresByCityAndPeriod(
            @Param("gameId") String gameId,
            @Param("city") String city,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 容灾恢复：统计指定时间段内，全国各省份的总通关数
     */
    @Query("SELECT u.province as regionName, COUNT(r) as score " +
           "FROM GameRecord r JOIN User u ON CAST(r.userId AS long) = u.id " +
           "WHERE r.gameId = :gameId AND u.province IS NOT NULL " +
           "AND r.createdAt >= :startTime AND r.createdAt < :endTime " +
           "GROUP BY u.province")
    List<Map<String, Object>> countProvinceScoresByPeriod(
            @Param("gameId") String gameId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 容灾恢复：统计指定时间段内，全国各城市的总通关数
     */
    @Query("SELECT u.city as regionName, COUNT(r) as score " +
           "FROM GameRecord r JOIN User u ON CAST(r.userId AS long) = u.id " +
           "WHERE r.gameId = :gameId AND u.city IS NOT NULL " +
           "AND r.createdAt >= :startTime AND r.createdAt < :endTime " +
           "GROUP BY u.city")
    List<Map<String, Object>> countCityScoresByPeriod(
            @Param("gameId") String gameId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
