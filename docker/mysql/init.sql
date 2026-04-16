CREATE DATABASE IF NOT EXISTS game_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE game_db;

-- --------------------------------------------------------
-- 1. 行政区划表 (Regions)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS regions (
  id INT NOT NULL COMMENT '行政区划代码',
  parent_id INT NOT NULL DEFAULT 0 COMMENT '父级代码',
  name VARCHAR(64) NOT NULL COMMENT '名称',
  level TINYINT NOT NULL COMMENT '层级: 1国家, 2省/州, 3市, 4区县',
  PRIMARY KEY (id),
  KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='国家省市区4级行政区划表';

-- --------------------------------------------------------
-- 2. 用户表 (Users)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户自增ID，主键',
  openid VARCHAR(64) NOT NULL COMMENT '微信小程序OpenID，唯一标识',
  unionid VARCHAR(64) NULL COMMENT '微信开放平台UnionID，跨应用标识',
  nickname VARCHAR(128) NULL COMMENT '用户微信昵称',
  avatar_url VARCHAR(512) NULL COMMENT '用户微信头像URL',
  country_id INT NULL COMMENT '用户所在国家ID',
  province_id INT NULL COMMENT '用户所在省/州ID',
  city_id INT NULL COMMENT '用户所在城市ID',
  district_id INT NULL COMMENT '用户所在区县ID',
  created_at DATETIME NOT NULL COMMENT '账号创建时间',
  last_login_at DATETIME NULL COMMENT '最后登录时间',
  PRIMARY KEY (id),
  UNIQUE KEY idx_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏玩家用户表';

-- --------------------------------------------------------
-- 3. 游戏表 (Games)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS games (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '游戏自增ID，主键',
  code VARCHAR(64) NOT NULL COMMENT '游戏代号（唯一，例如: sheep_game）',
  name VARCHAR(128) NOT NULL COMMENT '游戏名称',
  icon_url VARCHAR(512) NULL COMMENT '游戏图标URL',
  description VARCHAR(1024) NULL COMMENT '游戏描述',
  total_players BIGINT NOT NULL DEFAULT 0 COMMENT '全平台玩家数（汇总）',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏基础信息表';

-- --------------------------------------------------------
-- 4. 游戏平台信息表 (Game Platforms)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS game_platforms (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '平台信息自增ID，主键',
  game_id BIGINT NOT NULL COMMENT '游戏ID（关联games表id）',
  platform VARCHAR(32) NOT NULL COMMENT '平台标识（例如: ios/android/web）',
  version_name VARCHAR(64) NULL COMMENT '版本号（例如: 1.0.0）',
  download_url VARCHAR(512) NULL COMMENT '下载链接/应用商店链接',
  players BIGINT NOT NULL DEFAULT 0 COMMENT '该平台玩家数',
  created_at DATETIME NOT NULL COMMENT '创建时间',
  updated_at DATETIME NOT NULL COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY idx_game_platform (game_id, platform),
  CONSTRAINT fk_game_platforms_game_id FOREIGN KEY (game_id) REFERENCES games(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏各平台版本与链接信息表';

-- --------------------------------------------------------
-- 5. 游戏通关流水表 (Game Records)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS game_records (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '通关记录自增ID',
  user_id VARCHAR(64) NOT NULL COMMENT '用户ID (关联users表的id)',
  game_id BIGINT NOT NULL COMMENT '游戏ID (关联games表的id)',
  level_id VARCHAR(64) NOT NULL COMMENT '通关的关卡ID (例如: level_1)',
  sync_timestamp BIGINT NOT NULL COMMENT '客户端上报的通关时间戳(毫秒)',
  created_at DATETIME NOT NULL COMMENT '服务器落库时间',
  PRIMARY KEY (id),
  KEY idx_game_user (game_id, user_id),
  CONSTRAINT fk_game_records_game_id FOREIGN KEY (game_id) REFERENCES games(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游戏关卡通关流水表';

-- --------------------------------------------------------
-- 6. 插入初始测试数据 (Mock Data)
-- --------------------------------------------------------
-- 清理旧数据以防重复导入报错
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE game_records;
TRUNCATE TABLE game_platforms;
TRUNCATE TABLE games;
TRUNCATE TABLE users;
TRUNCATE TABLE regions;
SET FOREIGN_KEY_CHECKS = 1;

-- 插入4级(国家-省州-市区)基础数据
INSERT INTO regions (id, parent_id, name, level) VALUES
(1, 0, '中国', 1),
(110000, 1, '北京市', 2), (110100, 110000, '北京市', 3), (110105, 110100, '朝阳区', 4),
(310000, 1, '上海市', 2), (310100, 310000, '上海市', 3), (310115, 310100, '浦东新区', 4),
(330000, 1, '浙江省', 2), (330100, 330000, '杭州市', 3), (330106, 330100, '西湖区', 4),
(440000, 1, '广东省', 2), (440100, 440000, '广州市', 3), (440106, 440100, '天河区', 4),
(440300, 440000, '深圳市', 3), (440305, 440300, '南山区', 4),
(2, 0, '美国', 1),
(200100, 2, '加利福尼亚州', 2), (200101, 200100, '洛杉矶市', 3), (2001011, 200101, '好莱坞区', 4),
(200200, 2, '纽约州', 2), (200201, 200200, '纽约市', 3), (2002011, 200201, '曼哈顿区', 4);

-- 插入 6 个模拟玩家 (使用4级行政区划 ID)
INSERT INTO users (id, openid, nickname, avatar_url, country_id, province_id, city_id, district_id, created_at, last_login_at) VALUES 
(1, 'oWxMock_001_Guangdong_SZ', '深圳玩家A', 'https://mock.url/a.png', 1, 440000, 440300, 440305, NOW(), NOW()),
(2, 'oWxMock_002_Guangdong_GZ', '广州玩家B', 'https://mock.url/b.png', 1, 440000, 440100, 440106, NOW(), NOW()),
(3, 'oWxMock_003_Beijing_BJ', '北京玩家C', 'https://mock.url/c.png', 1, 110000, 110100, 110105, NOW(), NOW()),
(4, 'oWxMock_004_Shanghai_SH', '上海玩家D', 'https://mock.url/d.png', 1, 310000, 310100, 310115, NOW(), NOW()),
(5, 'oWxMock_005_Zhejiang_HZ', '杭州玩家E', 'https://mock.url/e.png', 1, 330000, 330100, 330106, NOW(), NOW()),
(6, 'oWxMock_006_USA_NY', '纽约玩家F', 'https://mock.url/f.png', 2, 200200, 200201, 2002011, NOW(), NOW());

-- 插入模拟游戏与平台信息
INSERT INTO games (id, code, name, icon_url, description, total_players, created_at, updated_at) VALUES
(1, 'sheep_game', '羊了个羊', 'https://mock.url/sheep.png', '一款轻松有趣的消除闯关游戏', 1200000, NOW(), NOW()),
(2, 'tower_defense', '塔防大战', 'https://mock.url/td.png', '策略塔防游戏', 560000, NOW(), NOW());

INSERT INTO game_platforms (game_id, platform, version_name, download_url, players, created_at, updated_at) VALUES
(1, 'ios', '1.2.0', 'https://apps.apple.com/app/id0000000001', 500000, NOW(), NOW()),
(1, 'android', '1.2.1', 'https://play.google.com/store/apps/details?id=com.mock.sheep', 700000, NOW(), NOW()),
(2, 'ios', '0.9.0', 'https://apps.apple.com/app/id0000000002', 200000, NOW(), NOW()),
(2, 'android', '0.9.1', 'https://play.google.com/store/apps/details?id=com.mock.td', 360000, NOW(), NOW());

-- 插入模拟通关记录 (假设 game_id 为 1)
-- 玩家 1 (深圳): 过了 3 关
INSERT INTO game_records (user_id, game_id, level_id, sync_timestamp, created_at) VALUES 
('1', 1, 'level_1', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 2 HOUR),
('1', 1, 'level_2', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 1 HOUR),
('1', 1, 'level_3', UNIX_TIMESTAMP() * 1000, NOW());

-- 玩家 2 (广州): 过了 5 关 (广东榜首)
INSERT INTO game_records (user_id, game_id, level_id, sync_timestamp, created_at) VALUES 
('2', 1, 'level_1', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 4 HOUR),
('2', 1, 'level_2', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 3 HOUR),
('2', 1, 'level_3', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 2 HOUR),
('2', 1, 'level_4', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 1 HOUR),
('2', 1, 'level_5', UNIX_TIMESTAMP() * 1000, NOW());

-- 玩家 3 (北京): 过了 4 关 (北京榜首)
INSERT INTO game_records (user_id, game_id, level_id, sync_timestamp, created_at) VALUES 
('3', 1, 'level_1', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 5 HOUR),
('3', 1, 'level_2', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 4 HOUR),
('3', 1, 'level_3', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 3 HOUR),
('3', 1, 'level_4', UNIX_TIMESTAMP() * 1000, NOW());

-- 玩家 4 (上海): 过了 2 关
INSERT INTO game_records (user_id, game_id, level_id, sync_timestamp, created_at) VALUES 
('4', 1, 'level_1', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 2 HOUR),
('4', 1, 'level_2', UNIX_TIMESTAMP() * 1000, NOW());

-- 玩家 5 (杭州): 过了 6 关 (全服榜首)
INSERT INTO game_records (user_id, game_id, level_id, sync_timestamp, created_at) VALUES 
('5', 1, 'level_1', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 6 HOUR),
('5', 1, 'level_2', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 5 HOUR),
('5', 1, 'level_3', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 4 HOUR),
('5', 1, 'level_4', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 3 HOUR),
('5', 1, 'level_5', UNIX_TIMESTAMP() * 1000, NOW() - INTERVAL 2 HOUR),
('5', 1, 'level_6', UNIX_TIMESTAMP() * 1000, NOW());
