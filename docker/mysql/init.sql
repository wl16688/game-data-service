CREATE DATABASE IF NOT EXISTS game_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE game_db;

-- --------------------------------------------------------
-- 1. 用户表 (Users)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户自增ID，主键',
  openid VARCHAR(64) NOT NULL COMMENT '微信小程序OpenID，唯一标识',
  unionid VARCHAR(64) NULL COMMENT '微信开放平台UnionID，跨应用标识',
  nickname VARCHAR(128) NULL COMMENT '用户微信昵称',
  avatar_url VARCHAR(512) NULL COMMENT '用户微信头像URL',
  province VARCHAR(64) NULL COMMENT '用户所在省份 (冗余用于排行榜)',
  city VARCHAR(64) NULL COMMENT '用户所在城市 (冗余用于排行榜)',
  created_at DATETIME NOT NULL COMMENT '账号创建时间',
  last_login_at DATETIME NULL COMMENT '最后登录时间',
  PRIMARY KEY (id),
  UNIQUE KEY idx_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏玩家用户表';

-- --------------------------------------------------------
-- 2. 游戏通关流水表 (Game Records)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS game_records (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '通关记录自增ID',
  user_id VARCHAR(64) NOT NULL COMMENT '用户ID (关联users表的id)',
  game_id VARCHAR(64) NOT NULL COMMENT '游戏应用ID (例如: sheep_game)',
  level_id VARCHAR(64) NOT NULL COMMENT '通关的关卡ID (例如: level_1)',
  sync_timestamp BIGINT NOT NULL COMMENT '客户端上报的通关时间戳(毫秒)',
  created_at DATETIME NOT NULL COMMENT '服务器落库时间',
  PRIMARY KEY (id),
  KEY idx_game_user (game_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏关卡通关流水表';

-- --------------------------------------------------------
-- 3. 插入初始测试数据 (Mock Data)
-- --------------------------------------------------------
-- 清理旧数据以防重复导入报错
TRUNCATE TABLE game_records;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入 5 个模拟玩家
INSERT INTO users (id, openid, nickname, avatar_url, province, city, created_at, last_login_at) VALUES 
(1, 'oWxMock_001_Guangdong_SZ', '深圳玩家A', 'https://mock.url/a.png', 'Guangdong', 'Shenzhen', NOW(), NOW()),
(2, 'oWxMock_002_Guangdong_GZ', '广州玩家B', 'https://mock.url/b.png', 'Guangdong', 'Guangzhou', NOW(), NOW()),
(3, 'oWxMock_003_Beijing_BJ', '北京玩家C', 'https://mock.url/c.png', 'Beijing', 'Beijing', NOW(), NOW()),
(4, 'oWxMock_004_Shanghai_SH', '上海玩家D', 'https://mock.url/d.png', 'Shanghai', 'Shanghai', NOW(), NOW()),
(5, 'oWxMock_005_Zhejiang_HZ', '杭州玩家E', 'https://mock.url/e.png', 'Zhejiang', 'Hangzhou', NOW(), NOW());

-- 插入模拟通关记录 (假设 game_id 为 'sheep_game')
-- 玩家 1 (深圳): 过了 3 关
INSERT INTO game_records (user_id, game_id, level_id, sync_timestamp, created_at) VALUES 
('1', 'sheep_game', 'level_1', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 2 HOUR),
('1', 'sheep_game', 'level_2', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 1 HOUR),
('1', 'sheep_game', 'level_3', UNIX_TIMESTAMP() * 1000, NOW());

-- 玩家 2 (广州): 过了 5 关 (广东榜首)
INSERT INTO game_records (user_id, game_id, level_id, sync_timestamp, created_at) VALUES 
('2', 'sheep_game', 'level_1', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 4 HOUR),
('2', 'sheep_game', 'level_2', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 3 HOUR),
('2', 'sheep_game', 'level_3', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 2 HOUR),
('2', 'sheep_game', 'level_4', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 1 HOUR),
('2', 'sheep_game', 'level_5', UNIX_TIMESTAMP() * 1000, NOW());

-- 玩家 3 (北京): 过了 4 关 (北京榜首)
INSERT INTO game_records (user_id, game_id, level_id, sync_timestamp, created_at) VALUES 
('3', 'sheep_game', 'level_1', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 5 HOUR),
('3', 'sheep_game', 'level_2', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 4 HOUR),
('3', 'sheep_game', 'level_3', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 3 HOUR),
('3', 'sheep_game', 'level_4', UNIX_TIMESTAMP() * 1000, NOW());

-- 玩家 4 (上海): 过了 2 关
INSERT INTO game_records (user_id, game_id, level_id, sync_timestamp, created_at) VALUES 
('4', 'sheep_game', 'level_1', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 2 HOUR),
('4', 'sheep_game', 'level_2', UNIX_TIMESTAMP() * 1000, NOW());

-- 玩家 5 (杭州): 过了 6 关 (全服榜首)
INSERT INTO game_records (user_id, game_id, level_id, sync_timestamp, created_at) VALUES 
('5', 'sheep_game', 'level_1', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 6 HOUR),
('5', 'sheep_game', 'level_2', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 5 HOUR),
('5', 'sheep_game', 'level_3', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 4 HOUR),
('5', 'sheep_game', 'level_4', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 3 HOUR),
('5', 'sheep_game', 'level_5', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 2 HOUR),
('5', 'sheep_game', 'level_6', UNIX_TIMESTAMP() * 1000, NOW());

