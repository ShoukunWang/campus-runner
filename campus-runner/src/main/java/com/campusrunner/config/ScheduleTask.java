package com.campusrunner.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusrunner.entity.Order;
import com.campusrunner.entity.Task;
import com.campusrunner.entity.User;
import com.campusrunner.mapper.OrderMapper;
import com.campusrunner.mapper.TaskMapper;
import com.campusrunner.mapper.UserMapper;
import com.campusrunner.service.CreditService;
import com.campusrunner.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ScheduleTask {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CreditService creditService;

    /**
     * 每天凌晨1点执行：自动确认超过3天未确认的任务
     * 
     * 业务规则：
     * 1. 跑腿员确认送达后，发布者有3天时间确认收到
     * 2. 超过3天未确认，系统自动确认完成，保护跑腿员权益
     * 3. 自动确认后自动创建订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoConfirmTasks() {
        System.out.println("[定时任务] 开始执行：自动确认超时任务 - " + LocalDateTime.now());
        
        // 查询送达时间超过3天的待确认任务
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getStatus, 3); // 待确认
        wrapper.le(Task::getDeliverTime, threeDaysAgo); // 送达超过3天
        
        List<Task> tasks = taskMapper.selectList(wrapper);
        System.out.println("[定时任务] 找到 " + tasks.size() + " 个超时待确认的任务");
        
        for (Task task : tasks) {
            try {
                // 1. 更新任务状态为已完成
                task.setStatus(4);
                task.setCompleteTime(LocalDateTime.now());
                task.setUpdateTime(LocalDateTime.now());
                taskMapper.updateById(task);
                
                // 2. 更新跑腿员统计数据
                User runner = userMapper.selectById(task.getRunnerId());
                if (runner != null) {
                    runner.setCompletedTasks(runner.getCompletedTasks() + 1);
                    runner.setTotalTasks(runner.getTotalTasks() + 1);
                    if (runner.getTotalTasks() > 0) {
                        runner.setCompletionRate(
                            new BigDecimal(runner.getCompletedTasks())
                                .divide(new BigDecimal(runner.getTotalTasks()), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                        );
                    }
                    userMapper.updateById(runner);
                }
                
                // 更新发布者统计
                User publisher = userMapper.selectById(task.getPublisherId());
                if (publisher != null) {
                    publisher.setTotalTasks(publisher.getTotalTasks() + 1);
                    userMapper.updateById(publisher);
                }
                
                // 3. 创建订单（直接传内存对象，避免缓存问题）
                orderService.createOrderFromTask(task);
                
                System.out.println("[定时任务] 任务ID=" + task.getId() + " 已自动确认完成");
            } catch (Exception e) {
                System.err.println("[定时任务] 任务ID=" + task.getId() + " 自动确认失败: " + e.getMessage());
            }
        }
        
        System.out.println("[定时任务] 自动确认任务执行完成");
    }

    /**
     * 每天凌晨1点10分执行：处理超过7天未支付的订单
     * 
     * 公平性设计：
     * 1. 不直接取消订单（否则跑腿员白干了）
     * 2. 标记为"支付逾期"状态，订单仍然保留可支付
     * 3. 扣除发布者信用积分
     * 4. 信用积分过低会影响发单权限
     */
    @Scheduled(cron = "0 10 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void handleOverdueOrders() {
        System.out.println("[定时任务] 开始执行：处理逾期未支付订单 - " + LocalDateTime.now());
        
        // 查询创建时间超过7天的待支付订单
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, 0); // 待支付
        wrapper.le(Order::getCreateTime, sevenDaysAgo); // 创建超过7天
        
        List<Order> orders = orderMapper.selectList(wrapper);
        System.out.println("[定时任务] 找到 " + orders.size() + " 个逾期未支付的订单");
        
        for (Order order : orders) {
            try {
                // 1. 标记为支付逾期状态（不是取消！）
                order.setStatus(4); // 支付逾期
                order.setUpdateTime(LocalDateTime.now());
                orderMapper.updateById(order);
                
                // 2. 扣除发布者信用积分（每次扣15分，对应PAYMENT_OVERDUE类型）
                try {
                    int newScore = creditService.deductScore(order.getPublisherId(), 15, "拒不支付", order.getId(), order.getTaskId());
                    System.out.println("[定时任务] 发布者ID=" + order.getPublisherId() + 
                                     " 信用积分因订单ID=" + order.getId() + "支付逾期被扣15分，当前分数: " + newScore);
                } catch (Exception e) {
                    System.err.println("[定时任务] 发布者ID=" + order.getPublisherId() + " 信用积分扣除失败: " + e.getMessage());
                }
                
                System.out.println("[定时任务] 订单ID=" + order.getId() + " 已标记为支付逾期");
            } catch (Exception e) {
                System.err.println("[定时任务] 订单ID=" + order.getId() + " 处理失败: " + e.getMessage());
            }
        }
        
        System.out.println("[定时任务] 逾期订单处理完成");
    }
}
