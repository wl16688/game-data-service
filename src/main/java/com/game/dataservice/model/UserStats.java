package com.game.dataservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {
    private String userId;
    private Long gameId;
    private Double totalLevels;  // 总通关数
    private Long dailyLevels;    // 当日通关数
    private Long globalRank;     // 全球排名
    private Long provinceRank;   // 省份排名
    private Long cityRank;       // 城市排名
}
