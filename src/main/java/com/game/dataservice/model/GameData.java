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
    private double score;
    private long timestamp;
}
