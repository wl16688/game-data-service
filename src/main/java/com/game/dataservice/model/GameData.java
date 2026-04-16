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
    private Long gameId;
    private String levelId; // 记录通关的关卡ID
    private Integer provinceId; // 用户所在省份ID
    private Integer cityId;     // 用户所在城市ID
    private Integer districtId; // 用户所在区县ID
    private long timestamp;
}
