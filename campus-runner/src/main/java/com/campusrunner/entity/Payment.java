package com.campusrunner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String paymentNo;

    private String taskId;

    private String payerId;

    private String payeeId;

    private BigDecimal amount;

    private Integer paymentType;

    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime refundTime;

    private String refundReason;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
