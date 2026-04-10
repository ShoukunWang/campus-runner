package com.campusrunner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String taskNo;

    private String publisherId;

    private String categoryId;

    private String title;

    private String description;

    private Integer taskType;

    private String pickupAddress;

    private BigDecimal pickupLongitude;

    private BigDecimal pickupLatitude;

    private String deliveryAddress;

    private BigDecimal deliveryLongitude;

    private BigDecimal deliveryLatitude;

    private BigDecimal reward;

    private BigDecimal weight;

    private LocalDateTime expectCompleteTime;

    private String contactName;

    private String contactPhone;

    private String remarks;

    private Integer status;

    private String runnerId;

    private LocalDateTime acceptTime;

    private LocalDateTime completeTime;

    private String cancelReason;

    private Integer viewCount;

    private Integer favoriteCount;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    @Version
    private Integer version;
}
