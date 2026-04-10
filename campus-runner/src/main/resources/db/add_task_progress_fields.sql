-- 为task表新增取件时间和送达时间字段
ALTER TABLE `task` 
ADD COLUMN `pickup_time` datetime DEFAULT NULL COMMENT '取件时间' AFTER `accept_time`,
ADD COLUMN `deliver_time` datetime DEFAULT NULL COMMENT '送达时间' AFTER `pickup_time`;

-- 为已有数据初始化( status=2 的已完成任务,设置送达时间为完成时间)
UPDATE `task` 
SET `deliver_time` = `complete_time`,
    `pickup_time` = `accept_time`
WHERE `status` = 2 AND `complete_time` IS NOT NULL;
