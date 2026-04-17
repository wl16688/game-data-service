package com.game.dataservice.controller.admin;

import com.game.dataservice.common.ApiResponse;
import com.game.dataservice.entity.Game;
import com.game.dataservice.entity.GamePlatform;
import com.game.dataservice.repository.GamePlatformRepository;
import com.game.dataservice.repository.GameRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/games")
@RequiredArgsConstructor
@Tag(name = "后台游戏管理", description = "后台管理系统：游戏的增删改查及各平台版本维护（需要 ADMIN JWT）")
@SecurityRequirement(name = "bearerAuth")
public class AdminGameController {

    private final GameRepository gameRepository;
    private final GamePlatformRepository gamePlatformRepository;

    @Operation(summary = "分页获取游戏列表")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Game>>> getGames(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Game> gamePage = gameRepository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(gamePage));
    }

    @Operation(summary = "新增游戏")
    @PostMapping
    public ResponseEntity<ApiResponse<Game>> createGame(@RequestBody Game game) {
        return ResponseEntity.ok(ApiResponse.success(gameRepository.save(game)));
    }

    @Operation(summary = "编辑游戏基本信息")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Game>> updateGame(
            @PathVariable Long id, 
            @RequestBody Game updatedGame) {
        return gameRepository.findById(id)
                .map(game -> {
                    game.setName(updatedGame.getName());
                    game.setCode(updatedGame.getCode());
                    game.setIconUrl(updatedGame.getIconUrl());
                    game.setDescription(updatedGame.getDescription());
                    return ResponseEntity.ok(ApiResponse.success(gameRepository.save(game)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "游戏不存在")));
    }

    @Operation(summary = "删除游戏")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteGame(@PathVariable Long id) {
        if (!gameRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error(404, "游戏不存在"));
        }
        // 注意：实际业务中可能需要级联删除 game_platforms 和 game_records
        // 此处先直接删除平台表，流水表可以视作软删除或不删除
        List<GamePlatform> platforms = gamePlatformRepository.findByGameId(id);
        gamePlatformRepository.deleteAll(platforms);
        
        gameRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @Operation(summary = "获取某游戏的平台信息列表")
    @GetMapping("/{gameId}/platforms")
    public ResponseEntity<ApiResponse<List<GamePlatform>>> getGamePlatforms(@PathVariable Long gameId) {
        return ResponseEntity.ok(ApiResponse.success(gamePlatformRepository.findByGameId(gameId)));
    }

    @Operation(summary = "新增游戏的平台信息")
    @PostMapping("/{gameId}/platforms")
    public ResponseEntity<ApiResponse<GamePlatform>> addGamePlatform(
            @PathVariable Long gameId,
            @RequestBody GamePlatform platform) {
        platform.setGameId(gameId);
        return ResponseEntity.ok(ApiResponse.success(gamePlatformRepository.save(platform)));
    }

    @Operation(summary = "更新游戏的平台信息")
    @PutMapping("/{gameId}/platforms/{platformId}")
    public ResponseEntity<ApiResponse<GamePlatform>> updateGamePlatform(
            @PathVariable Long gameId,
            @PathVariable Long platformId,
            @RequestBody GamePlatform platformDetails) {
        return gamePlatformRepository.findById(platformId)
                .map(platform -> {
                    platform.setPlatform(platformDetails.getPlatform());
                    platform.setVersion(platformDetails.getVersion());
                    platform.setDownloadUrl(platformDetails.getDownloadUrl());
                    return ResponseEntity.ok(ApiResponse.success(gamePlatformRepository.save(platform)));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "平台信息不存在")));
    }

    @Operation(summary = "删除游戏的平台信息")
    @DeleteMapping("/{gameId}/platforms/{platformId}")
    public ResponseEntity<ApiResponse<String>> deleteGamePlatform(
            @PathVariable Long gameId,
            @PathVariable Long platformId) {
        gamePlatformRepository.deleteById(platformId);
        return ResponseEntity.ok(ApiResponse.success("删除平台信息成功", null));
    }
}