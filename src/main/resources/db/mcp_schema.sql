-- MCP 管理系统数据库表结构

USE `spring_ai_demo`;

-- 创建 MCP 工具表
CREATE TABLE IF NOT EXISTS `mcp_tool` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '工具名称',
  `description` TEXT COMMENT '工具描述',
  `type` VARCHAR(20) NOT NULL COMMENT '工具类型：LOCAL-本地, REMOTE-远程',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED-启用, DISABLED-禁用',
  `config_json` TEXT COMMENT '配置信息（JSON格式）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  INDEX `idx_type` (`type`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MCP工具表';

-- 创建 MCP 市场表
CREATE TABLE IF NOT EXISTS `mcp_market` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '市场名称',
  `url` VARCHAR(500) NOT NULL COMMENT '市场URL',
  `description` TEXT COMMENT '市场描述',
  `auth_config` TEXT COMMENT '认证配置（JSON格式）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED-启用, DISABLED-禁用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MCP市场表';

-- 创建 MCP 市场工具关联表
CREATE TABLE IF NOT EXISTS `mcp_market_tool` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `market_id` BIGINT NOT NULL COMMENT '市场ID',
  `tool_name` VARCHAR(100) NOT NULL COMMENT '工具名称',
  `tool_description` TEXT COMMENT '工具描述',
  `tool_version` VARCHAR(50) COMMENT '工具版本',
  `tool_metadata` TEXT COMMENT '工具元数据（JSON格式）',
  `is_loaded` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已加载到本地：0-未加载, 1-已加载',
  `local_tool_id` BIGINT COMMENT '关联的本地工具ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_market_id` (`market_id`),
  INDEX `idx_is_loaded` (`is_loaded`),
  INDEX `idx_tool_name` (`tool_name`),
  FOREIGN KEY (`market_id`) REFERENCES `mcp_market` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MCP市场工具表';

