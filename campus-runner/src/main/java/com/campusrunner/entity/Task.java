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

    /**
     * 任务状态：
     * -1 - 待发布（草稿）：发布者暂存，可修改后再发布
     *  0 - 待接取：已发布，任务大厅可见
     *  1 - 进行中：跑腿员已接单，待提货
     *  2 - 已提货：跑腿员已提货，配送中
     *  3 - 待确认：已送达，等待发布者确认
     *  4 - 已完成：发布者已确认收到，订单已创建
     *  5 - 已取消：发布者取消发布，回到草稿状态
     *  6 - 已退回：跑腿员取消，退回任务大厅
     */
    private Integer status;

    private String runnerId;

    private LocalDateTime acceptTime;

    private LocalDateTime pickupTime;

    private LocalDateTime deliverTime;

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
