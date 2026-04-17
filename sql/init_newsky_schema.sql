-- NewSky 插件 MySQL 初始化（依据 source/lib/*Lib 下 Info.hbm.xml 与 QuickShop DatabaseHelper）
-- 字符集：utf8mb4（与旧版 utf8 数据兼容迁移时可用 mysqldump 转换）
--
-- 与「其他服务共用一台 MySQL」时：
-- 1) 删除下面 CREATE DATABASE / USE，在已有库中执行；或
-- 2) 将 newsky 改为你的库名后整段执行。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `newsky`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `newsky`;

-- ========== land 插件 ==========

DROP TABLE IF EXISTS `land_entertip`;
CREATE TABLE `land_entertip` (
  `id` bigint NOT NULL,
  `landId` bigint NOT NULL,
  `tip` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_land_entertip_landId` (`landId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `land_leavetip`;
CREATE TABLE `land_leavetip` (
  `id` bigint NOT NULL,
  `landId` bigint NOT NULL,
  `tip` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_land_leavetip_landId` (`landId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `land_landuser`;
CREATE TABLE `land_landuser` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `maxLands` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_land_landuser_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `land_land`;
CREATE TABLE `land_land` (
  `id` bigint NOT NULL,
  `fix` tinyint(1) DEFAULT NULL,
  `type` int DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `overlap` tinyint(1) DEFAULT NULL,
  `price` int DEFAULT NULL,
  `owner` varchar(255) DEFAULT NULL,
  `range_` longblob,
  `level` int DEFAULT NULL,
  `flags` longblob,
  `pers` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `land_landspawn`;
CREATE TABLE `land_landspawn` (
  `id` bigint NOT NULL,
  `landId` bigint NOT NULL,
  `spawn` longblob,
  `yaw` float DEFAULT NULL,
  `pitch` float DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_land_landspawn_landId` (`landId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `land_landcmd`;
CREATE TABLE `land_landcmd` (
  `id` bigint NOT NULL,
  `landId` bigint NOT NULL,
  `cmd` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_land_landcmd_landId` (`landId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== house 插件 ==========

DROP TABLE IF EXISTS `houseuser`;
CREATE TABLE `houseuser` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `x` int DEFAULT NULL,
  `z` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_houseuser_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== lib 插件（user 为保留字，须反引号）==========

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_user_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `debtuser`;
CREATE TABLE `debtuser` (
  `id` bigint NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `debt` int DEFAULT NULL,
  `log` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== ticket 插件 ==========

DROP TABLE IF EXISTS `ticket`;
CREATE TABLE `ticket` (
  `id` bigint NOT NULL,
  `code` varchar(255) DEFAULT NULL,
  `status` int DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  `createTime` bigint DEFAULT NULL,
  `useTime` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ticket_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== QuickShop（config.yml 中 database.mysql: true 时使用）==========
-- 引用：org.maxgamer.quickshop.Database.DatabaseHelper

DROP TABLE IF EXISTS `shops`;
CREATE TABLE `shops` (
  `owner` varchar(20) NOT NULL,
  `price` double NOT NULL,
  `itemConfig` blob NOT NULL,
  `x` int NOT NULL,
  `y` int NOT NULL,
  `z` int NOT NULL,
  `world` varchar(32) NOT NULL,
  `unlimited` tinyint(1) DEFAULT NULL,
  `type` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`x`,`y`,`z`,`world`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages` (
  `owner` varchar(20) NOT NULL,
  `message` varchar(200) NOT NULL,
  `time` bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
