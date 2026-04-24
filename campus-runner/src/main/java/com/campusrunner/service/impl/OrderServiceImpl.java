package com.campusrunner.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusrunner.entity.Order;
import com.campusrunner.entity.Task;
import com.campusrunner.mapper.OrderMapper;
import com.campusrunner.mapper.TaskMapper;
import com.campusrunner.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Order createOrderFromTask(Task task) {
        System.out.println("========================================");
        System.out.println("[订单创建] 开始，任务ID: " + task.getId());
        System.out.println("[订单创建] 任务状态（内存中）: " + task.getStatus());

        // 校验任务状态：只有已完成(4)的任务才能创建订单
        if (task.getStatus() != 4) {
            System.err.println("[订单创建] 失败：任务状态不正确，需要4，实际是: " + task.getStatus());
            throw new RuntimeException("只有发布者确认完成的任务才能创建订单，当前状态=" + task.getStatus());
        }

        // 检查是否已存在订单
        Order existingOrder = getOrderByTaskId(task.getId());
        if (existingOrder != null) {
            System.err.println("[订单创建] 失败：该任务已创建订单，订单ID: " + existingOrder.getId());
            throw new RuntimeException("该任务已创建订单");
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setTaskId(task.getId());
        order.setTaskNo(task.getTaskNo());
        order.setPublisherId(task.getPublisherId());
        order.setRunnerId(task.getRunnerId());
        order.setAmount(task.getReward());
        order.setStatus(0); // 待支付
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        System.out.println("[订单创建] 准备插入数据库，表名: task_order");
        baseMapper.insert(order);
        System.out.println("[订单创建] 成功！订单ID: " + order.getId() + ", 订单号: " + order.getOrderNo());
        System.out.println("========================================");
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrderFromTask(String taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        return createOrderFromTask(task);
    }

    @Override
    @Transactional
    public Order payOrder(String orderId, String userId) {
        // 查询订单
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 校验订单状态
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态异常，只有待支付订单可以支付");
        }

        // 校验是否为付款人
        if (!order.getPublisherId().equals(userId)) {
            throw new RuntimeException("只有订单创建者可以支付");
        }

        // 更新订单状态
        order.setStatus(1); // 已支付
        order.setPayTime(LocalDateTime.now());
        order.setPaymentNo(generatePaymentNo());
        order.setPaymentType(1); // 任务酬金
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);

        return order;
    }

    @Override
    @Transactional
    public Order cancelOrder(String orderId, String userId, String reason) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 只有待支付状态可以取消
        if (order.getStatus() != 0) {
            throw new RuntimeException("只有待支付订单可以取消");
        }

        // ====================== 公平性设计 ======================
        // 任务既然已经确认完成（双方无争议才创建的订单），发布者就无权取消了！
        // 只能由跑腿员主动取消（跑腿员放弃赏金）或由管理员后台仲裁
        // ========================================================
        
        // 校验权限：只有跑腿员可以取消订单（发布者不能取消！）
        if (!order.getRunnerId().equals(userId)) {
            throw new RuntimeException("只有跑腿员可以取消订单。若有争议请联系平台客服仲裁");
        }

        order.setStatus(2); // 已取消
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);

        return order;
    }

    @Override
    @Transactional
    public Order applyRefund(String orderId, String userId, String reason) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 只有已支付状态可以申请退款
        if (order.getStatus() != 1) {
            throw new RuntimeException("只有已支付订单可以申请退款");
        }

        // 校验权限（只有发布者可以申请退款）
        if (!order.getPublisherId().equals(userId)) {
            throw new RuntimeException("只有发布者可以申请退款");
        }

        order.setStatus(2); // 退款中 - 待跑腿员审核
        order.setRefundTime(LocalDateTime.now());
        order.setRefundReason(reason);
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(order);

        return order;
    }

    @Override
    @Transactional
    public Order auditRefund(String orderId, String userId, Boolean agreed, String rejectReason) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 只有退款中状态可以审核
        if (order.getStatus() != 2) {
            throw new RuntimeException("只有退款中状态的订单可以审核");
        }

        // 校验权限（只有跑腿员可以审核退款）
        if (!order.getRunnerId().equals(userId)) {
            throw new RuntimeException("只有跑腿员可以审核退款申请");
        }

        if (agreed) {
            // 同意退款
            order.setStatus(3); // 已退款
            order.setUpdateTime(LocalDateTime.now());
        } else {
            // 拒绝退款，回到已支付状态
            order.setStatus(1); // 回到已支付
            order.setRefundReason("拒绝退款：" + (rejectReason != null ? rejectReason : "跑腿员不同意退款"));
            order.setUpdateTime(LocalDateTime.now());
        }

        baseMapper.updateById(order);
        return order;
    }

    @Override
    public Page<Order> getMyOrders(String userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // 查询我是发布者或跑腿员的订单
        wrapper.and(w -> w.eq(Order::getPublisherId, userId).or().eq(Order::getRunnerId, userId));

        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Order getOrderByTaskId(String taskId) {
        return baseMapper.selectOne(
            new LambdaQueryWrapper<Order>().eq(Order::getTaskId, taskId)
        );
    }

    @Override
    public Map<String, Object> getOrderStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 如果userId为null，返回空统计
        if (userId == null) {
            stats.put("pendingCount", 0);
            stats.put("incomeCount", 0);
            stats.put("incomeAmount", BigDecimal.ZERO);
            stats.put("expenseCount", 0);
            stats.put("expenseAmount", BigDecimal.ZERO);
            stats.put("completedCount", 0);
            return stats;
        }

        // 总收入（作为跑腿员已支付的订单）
        LambdaQueryWrapper<Order> incomeWrapper = new LambdaQueryWrapper<>();
        incomeWrapper.eq(Order::getRunnerId, userId);
        incomeWrapper.eq(Order::getStatus, 1); // 已支付
        long incomeCount = baseMapper.selectCount(incomeWrapper);
        stats.put("incomeCount", incomeCount);

        // 使用SQL聚合函数计算总收入
        List<Order> incomeOrders = baseMapper.selectList(incomeWrapper);
        BigDecimal incomeAmount = incomeOrders.stream()
            .map(Order::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("incomeAmount", incomeAmount);

        // 总支出（作为发布者已支付的订单）
        LambdaQueryWrapper<Order> expenseWrapper = new LambdaQueryWrapper<>();
        expenseWrapper.eq(Order::getPublisherId, userId);
        expenseWrapper.eq(Order::getStatus, 1);
        long expenseCount = baseMapper.selectCount(expenseWrapper);
        stats.put("expenseCount", expenseCount);

        List<Order> expenseOrders = baseMapper.selectList(expenseWrapper);
        BigDecimal expenseAmount = expenseOrders.stream()
            .map(Order::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("expenseAmount", expenseAmount);

        // 待支付订单数
        LambdaQueryWrapper<Order> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Order::getPublisherId, userId);
        pendingWrapper.eq(Order::getStatus, 0);
        long pendingCount = baseMapper.selectCount(pendingWrapper);
        stats.put("pendingCount", pendingCount);

        // 已完成订单数（包括已支付、已取消、已退款）
        LambdaQueryWrapper<Order> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.and(w -> w.eq(Order::getPublisherId, userId).or().eq(Order::getRunnerId, userId));
        completedWrapper.in(Order::getStatus, 1, 2, 3);
        long completedCount = baseMapper.selectCount(completedWrapper);
        stats.put("completedCount", completedCount);

        return stats;
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
               String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 生成支付流水号
     */
    private String generatePaymentNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
               String.format("%04d", (int)(Math.random() * 10000));
    }
}
