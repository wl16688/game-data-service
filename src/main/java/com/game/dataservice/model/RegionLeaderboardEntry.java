package com.game.dataservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionLeaderboardEntry {
    private String regionName; // 可以是省份名称或城市名称
    private double totalLevels; // 该地区所有玩家的累计通关数
    private long rank; // 该地区在全球或全国的排名
}
