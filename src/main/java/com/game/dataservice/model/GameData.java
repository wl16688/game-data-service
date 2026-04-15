package com.game.dataservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameData implements Serializable {
    private String userId;
    private String gameId;
    private String levelId; // 记录通关的关卡ID
    private String province; // 用户所在省份
    private String city;     // 用户所在城市
    private long timestamp;
}
