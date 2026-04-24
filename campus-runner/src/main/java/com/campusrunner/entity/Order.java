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

    /**
     * 订单状态：
     * 0 - 待支付：等待发布者支付赏金
     * 1 - 已支付：发布者已完成支付
     * 2 - 退款中：发布者申请退款，待跑腿员审核
     * 3 - 已退款：跑腿员已同意退款
     * 4 - 支付逾期：超过7天未支付，已扣信用积分
     */
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
