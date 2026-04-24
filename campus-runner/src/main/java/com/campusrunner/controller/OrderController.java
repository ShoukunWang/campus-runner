package com.campusrunner.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.Order;
import com.campusrunner.service.OrderService;
import com.campusrunner.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单（任务完成时自动调用）
     */
    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String taskId = (String) params.get("taskId");

            if (taskId == null) {
                return Result.error(400, "缺少任务ID");
            }

            Order order = orderService.createOrderFromTask(taskId);
            return Result.success("订单创建成功", order);
        } catch (RuntimeException e) {
            return Result.error(7001, e.getMessage());
        }
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay/{orderId}")
    public Result<Order> payOrder(@PathVariable String orderId, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            Order order = orderService.payOrder(orderId, userId);
            return Result.success("支付成功", order);
        } catch (RuntimeException e) {
            return Result.error(7002, e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderId}")
    public Result<Order> cancelOrder(@PathVariable String orderId, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String reason = (String) params.get("reason");
            Order order = orderService.cancelOrder(orderId, userId, reason);
            return Result.success("订单已取消", order);
        } catch (RuntimeException e) {
            return Result.error(7003, e.getMessage());
        }
    }

    /**
     * 退款订单
     */
    @PostMapping("/refund/{orderId}")
    public Result<Order> refundOrder(@PathVariable String orderId, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String reason = (String) params.get("reason");
            Order order = orderService.refundOrder(orderId, userId, reason);
            return Result.success("退款成功", order);
        } catch (RuntimeException e) {
            return Result.error(7004, e.getMessage());
        }
    }

    /**
     * 查询我的订单
     */
    @GetMapping("/list")
    public Result<Page<Order>> getMyOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        Page<Order> page = orderService.getMyOrders(userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 根据任务ID查询订单
     */
    @GetMapping("/task/{taskId}")
    public Result<Order> getOrderByTask(@PathVariable String taskId) {
        Order order = orderService.getOrderByTaskId(taskId);
        // 订单不存在时返回null，而不是错误
        // 因为任务刚完成时，订单可能还在创建中
        return Result.success(order);
    }

    /**
     * 订单统计
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getOrderStatistics(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            if (userId == null) {
                return Result.error(401, "未登录");
            }
            Map<String, Object> stats = orderService.getOrderStatistics(userId);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(7006, "获取订单统计失败: " + e.getMessage());
        }
    }
}
