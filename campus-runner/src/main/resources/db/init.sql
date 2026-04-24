-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_runner DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_runner;

-- 用户表
CREATE TABLE `user` (
    `id` VARCHAR(32) NOT NULL,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '加密后的密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` TINYINT DEFAULT 0 COMMENT '角色：0-发布者，1-跑腿员，2-两者皆可',
    `credit_score` INT DEFAULT 100 COMMENT '信用积分',
    `completion_rate` DECIMAL(5,2) DEFAULT 100.00 COMMENT '任务完成率(%)',
    `total_tasks` INT DEFAULT 0 COMMENT '发布任务总数',
    `completed_tasks` INT DEFAULT 0 COMMENT '完成任务总数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_by` VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_credit_score` (`credit_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 管理员表
CREATE TABLE `admin` (
    `id` VARCHAR(32) NOT NULL,
    `username` VARCHAR(50) NOT NULL COMMENT '管理员账号',
    `password` VARCHAR(100) NOT NULL COMMENT '加密后的密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role` TINYINT DEFAULT 1 COMMENT '管理员角色：1-超级管理员，2-普通管理员',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `create_by` VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 任务分类表
CREATE TABLE `task_category` (
    `id` VARCHAR(32) NOT NULL,
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` VARCHAR(32) DEFAULT NULL COMMENT '父分类ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序号',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_by` VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务分类表';

-- 任务表
CREATE TABLE `task` (
    `id` VARCHAR(32) NOT NULL,
    `task_no` VARCHAR(32) NOT NULL COMMENT '任务编号',
    `publisher_id` VARCHAR(32) NOT NULL COMMENT '发布者用户ID',
    `category_id` VARCHAR(32) NOT NULL COMMENT '任务分类ID',
    `title` VARCHAR(100) NOT NULL COMMENT '任务标题',
    `description` TEXT COMMENT '任务详细描述',
    `task_type` TINYINT DEFAULT 1 COMMENT '任务类型：1-代取快递，2-代购，3-跨校区送件，4-其他',
    `pickup_address` VARCHAR(255) DEFAULT NULL COMMENT '取件地址',
    `pickup_longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '取件地址经度',
    `pickup_latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '取件地址纬度',
    `delivery_address` VARCHAR(255) DEFAULT NULL COMMENT '送达地址',
    `delivery_longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '送达地址经度',
    `delivery_latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '送达地址纬度',
    `reward` DECIMAL(10,2) NOT NULL COMMENT '任务酬金（元）',
    `weight` DECIMAL(8,2) DEFAULT 0 COMMENT '物品重量（kg）',
    `expect_complete_time` DATETIME DEFAULT NULL COMMENT '期望完成时间',
    `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
    `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `status` TINYINT DEFAULT -1 COMMENT '任务状态：-1-待发布（草稿），0-待接取，1-进行中（已接单），2-已提货，3-待确认，4-已完成，5-已取消，6-已退回',
    `runner_id` VARCHAR(32) DEFAULT NULL COMMENT '接单跑腿员ID',
    `accept_time` DATETIME DEFAULT NULL COMMENT '接单时间',
    `pickup_time` DATETIME DEFAULT NULL COMMENT '取件时间', -- 新增字段
    `deliver_time` DATETIME DEFAULT NULL COMMENT '送达时间', -- 新增字段
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `favorite_count` INT DEFAULT 0 COMMENT '收藏次数',
    `create_by` VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `version` INT DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_no` (`task_no`),
    KEY `idx_publisher_id` (`publisher_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_runner_id` (`runner_id`),
    KEY `idx_reward` (`reward`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 订单表
CREATE TABLE `task_order` (
    `id` VARCHAR(32) NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
    `task_id` VARCHAR(32) NOT NULL COMMENT '任务ID',
    `task_no` VARCHAR(32) NOT NULL COMMENT '任务编号',
    `publisher_id` VARCHAR(32) NOT NULL COMMENT '发布者ID（付款人）',
    `runner_id` VARCHAR(32) NOT NULL COMMENT '跑腿员ID（收款人）',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额（元）',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-退款中，3-已退款，4-支付逾期（已扣信用分，仍可支付）',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
    `refund_reason` VARCHAR(255) DEFAULT NULL COMMENT '退款原因',
    `payment_no` VARCHAR(32) DEFAULT NULL COMMENT '支付流水号',
    `payment_type` TINYINT DEFAULT 1 COMMENT '支付类型：1-任务酬金，2-平台服务费',
    `create_by` VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    UNIQUE KEY `uk_task_id` (`task_id`),
    KEY `idx_publisher_id` (`publisher_id`),
    KEY `idx_runner_id` (`runner_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 收藏表
CREATE TABLE `favorite` (
    `id` VARCHAR(32) NOT NULL,
    `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
    `task_id` VARCHAR(32) NOT NULL COMMENT '任务ID',
    `create_by` VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_task` (`user_id`, `task_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 评价表
CREATE TABLE `review` (
    `id` VARCHAR(32) NOT NULL,
    `task_id` VARCHAR(32) NOT NULL COMMENT '任务ID',
    `reviewer_id` VARCHAR(32) NOT NULL COMMENT '评价人ID',
    `reviewee_id` VARCHAR(32) NOT NULL COMMENT '被评价人ID',
    `review_type` TINYINT NOT NULL COMMENT '评价类型：1-发布者对跑腿员，2-跑腿员对发布者',
    `rating` TINYINT NOT NULL COMMENT '评分：1-5星',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT '评价标签，逗号分隔',
    `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
    `create_by` VARCHAR(32) DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(32) DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_reviewer` (`task_id`, `reviewer_id`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_reviewer_id` (`reviewer_id`),
    KEY `idx_reviewee_id` (`reviewee_id`),
    KEY `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';


-- 初始化管理员账号（密码：admin123）
-- 使用 Hutool DigestUtil.bcrypt("admin123") 生成的哈希
INSERT INTO `admin` (`id`, `username`, `password`, `real_name`, `role`, `status`) 
VALUES ('1', 'admin', '$2a$10$7JB720yubVSf6hvsS5qQ0uQ9e5G5qQ0uQ9e5G5qQ0uQ9e5G5qQ0uQ9e', '超级管理员', 1, 1)
ON DUPLICATE KEY UPDATE password = '$2a$10$7JB720yubVSf6hvsS5qQ0uQ9e5G5qQ0uQ9e5G5qQ0uQ9e5G5qQ0uQ9e';

-- 初始化任务分类
INSERT INTO `task_category` (`id`, `name`, `sort_order`, `status`) VALUES
('1', '代取快递', 1, 1),
('2', '代购', 2, 1),
('3', '跨校区送件', 3, 1),
('4', '文件传递', 4, 1),
('5', '其他', 5, 1);

-- 初始化系统配置（智能匹配算法权重）
INSERT INTO `sys_config` (`id`, `config_key`, `config_value`, `description`) VALUES
('1', 'match.weight.distance', '0.4', '距离权重'),
('2', 'match.weight.credit', '0.3', '信用积分权重'),
('3', 'match.weight.reward', '0.2', '酬金权重'),
('4', 'match.weight.completion', '0.1', '完成率权重');
