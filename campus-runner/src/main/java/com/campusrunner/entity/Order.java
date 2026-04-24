package com.campusrunner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("task_order")
public class Order {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String orderNo;

    private String taskId;

    private String taskNo;

    private String publisherId;

    private String runnerId;

    private BigDecimal amount;

    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime cancelTime;

    private String cancelReason;

    private LocalDateTime refundTime;

    private String refundReason;

    private String paymentNo;

    private Integer paymentType;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
