-- Chat Memory Table for LangChain4j persistence
CREATE TABLE IF NOT EXISTS `chat_memory` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `user_id` VARCHAR(128) NOT NULL COMMENT 'User/memory identifier',
    `messages` TEXT NOT NULL COMMENT 'Serialized ChatMessage list (JSON)',
    `create_user` VARCHAR(128) DEFAULT NULL COMMENT 'Creator',
    `create_date_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    `update_user` VARCHAR(128) DEFAULT NULL COMMENT 'Last updater',
    `update_date_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Soft delete flag: 0=active, 1=deleted',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_deleted` (`user_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chat memory persistence store';
