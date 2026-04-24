package com.campusrunner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusrunner.entity.Order;
import com.campusrunner.entity.Task;

import java.util.Map;

public interface OrderService extends IService<Order> {

    /**
     * 任务完成时自动创建订单
     */
    Order createOrderFromTask(Task task);

    Order createOrderFromTask(String taskId);

    /**
     * 支付订单
     */
    Order payOrder(String orderId, String userId);

    /**
     * 取消订单
     */
    Order cancelOrder(String orderId, String userId, String reason);

    /**
     * 申请退款
     */
    Order applyRefund(String orderId, String userId, String reason);

    /**
     * 审核退款
     */
    Order auditRefund(String orderId, String userId, Boolean agreed, String rejectReason);

    /**
     * 查询我的订单
     */
    Page<Order> getMyOrders(String userId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 根据任务ID查询订单
     */
    Order getOrderByTaskId(String taskId);

    /**
     * 订单统计
     */
    Map<String, Object> getOrderStatistics(String userId);
}
