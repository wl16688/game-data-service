package com.game.dataservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_platforms", indexes = {
        @Index(name = "idx_game_platform", columnList = "game_id, platform", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GamePlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "platform", nullable = false, length = 32)
    private String platform;

    @Column(name = "version_name", length = 64)
    private String versionName;

    @Column(name = "download_url", length = 512)
    private String downloadUrl;

    @Column(name = "players", nullable = false)
    private long players;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

