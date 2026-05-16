package com.campusrunner.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("credit_record")
public class CreditRecord {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private Integer changeAmount;

    private Integer beforeScore;

    private Integer afterScore;

    private Integer type;

    private String reason;

    private String relatedOrderId;

    private String relatedTaskId;

    private LocalDateTime createTime;
}