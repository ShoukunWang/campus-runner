package com.campusrunner.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("appeal")
public class Appeal {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String orderId;

    private String taskId;

    private String applicantId;

    private String respondentId;

    private Integer appealType;

    private Integer status;

    private BigDecimal compensationAmount;

    private String evidence;

    private String adminId;

    private String auditRemark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}