package com.campusrunner.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusrunner.entity.Task;
import com.campusrunner.entity.User;
import com.campusrunner.mapper.TaskMapper;
import com.campusrunner.mapper.UserMapper;
import com.campusrunner.service.CreditService;
import com.campusrunner.service.OrderService;
import com.campusrunner.service.TaskService;
import com.campusrunner.utils.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SnowflakeIdUtil snowflakeIdUtil;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CreditService creditService;

    @Override
    public Page<Task> getTaskList(Integer pageNum, Integer pageSize, String categoryId, Integer taskType,
                                 BigDecimal minReward, BigDecimal maxReward, String area, String keyword, String sortField) {
        Page<Task> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        
        // 查询待接取(0)和已退回(6)状态的任务，在大厅显示
        wrapper.in(Task::getStatus, Arrays.asList(0, 6));
        
        if (StrUtil.isNotBlank(categoryId)) {
            wrapper.eq(Task::getCategoryId, categoryId);
        }
        if (taskType != null) {
            wrapper.eq(Task::getTaskType, taskType);
        }
        if (minReward != null) {
            wrapper.ge(Task::getReward, minReward);
        }
        if (maxReward != null) {
            wrapper.le(Task::getReward, maxReward);
        }
        if (StrUtil.isNotBlank(area)) {
            wrapper.and(w -> w.like(Task::getPickupAddress, area).or().like(Task::getDeliveryAddress, area));
        }
        
        // 关键词搜索：标题、描述、地址
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w
                .like(Task::getTitle, keyword)
                .or()
                .like(Task::getDescription, keyword)
                .or()
                .like(Task::getPickupAddress, keyword)
                .or()
                .like(Task::getDeliveryAddress, keyword)
            );
        }
        
        if ("rewardAsc".equals(sortField)) {
            wrapper.orderByAsc(Task::getReward);
        } else if ("rewardDesc".equals(sortField)) {
            wrapper.orderByDesc(Task::getReward);
        } else {
            wrapper.orderByDesc(Task::getCreateTime);
        }
        
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Task getTaskDetail(String taskId) {
        Task task = baseMapper.selectById(taskId);
        if (task != null) {
            baseMapper.incrementViewCount(taskId);
        }
        return task;
    }

    @Override
    @Transactional
    public Task publishTask(String publisherId, String categoryId, String title, String description,
                           Integer taskType, String pickupAddress, BigDecimal pickupLongitude, BigDecimal pickupLatitude,
                           String deliveryAddress, BigDecimal deliveryLongitude, BigDecimal deliveryLatitude,
                           BigDecimal reward, BigDecimal weight, String expectCompleteTime,
                           String contactName, String contactPhone, String remarks) {
        User publisher = userMapper.selectById(publisherId);
        if (publisher == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!creditService.canPublishTask(publisher.getCreditScore())) {
            throw new RuntimeException("信誉分过低，无法发布任务");
        }

        Task task = new Task();
        task.setTaskNo(snowflakeIdUtil.generateTaskNo());
        task.setPublisherId(publisherId);
        task.setCategoryId(categoryId);
        task.setTitle(title);
        task.setDescription(description);
        task.setTaskType(taskType);
        task.setPickupAddress(pickupAddress);
        task.setPickupLongitude(pickupLongitude);
        task.setPickupLatitude(pickupLatitude);
        task.setDeliveryAddress(deliveryAddress);
        task.setDeliveryLongitude(deliveryLongitude);
        task.setDeliveryLatitude(deliveryLatitude);
        task.setReward(reward);
        task.setWeight(weight != null ? weight : BigDecimal.ZERO);
        task.setExpectCompleteTime(LocalDateTime.parse(expectCompleteTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        task.setContactName(contactName);
        task.setContactPhone(contactPhone);
        task.setRemarks(remarks);
        task.setStatus(0);
        task.setViewCount(0);
        task.setFavoriteCount(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        task.setVersion(0);
        
        baseMapper.insert(task);
        
        User user = userMapper.selectById(publisherId);
        if (user != null) {
            user.setTotalTasks(user.getTotalTasks() + 1);
            userMapper.updateById(user);
        }
        
        return task;
    }

    @Override
    @Transactional
    public Task acceptTask(String taskId, String runnerId) {
        Task task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        // 允许待接取(0)和已退回(6)状态的任务被接单
        if (task.getStatus() != 0 && task.getStatus() != 6) {
            throw new RuntimeException("该任务不可被接取");
        }
        if (task.getPublisherId().equals(runnerId)) {
            throw new RuntimeException("不能接自己发布的任务");
        }
        
        User runner = userMapper.selectById(runnerId);
        if (runner == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!creditService.canAcceptTask(runner.getCreditScore())) {
            throw new RuntimeException("信誉分过低，无法接单");
        }
        
        task.setRunnerId(runnerId);
        task.setStatus(1);
        task.setAcceptTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        
        int updated = baseMapper.updateById(task);
        if (updated == 0) {
            throw new RuntimeException("任务已被其他跑腿员接单");
        }
        
        return task;
    }

    @Override
    @Transactional
    public Task updateTaskStatus(String taskId, String action, String cancelReason, String operatorId) {
        Task task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        switch (action) {
            // ========== 发布者操作 ==========
            case "publish":
                // 发布草稿任务
                if (task.getStatus() != -1 && task.getStatus() != 5) {
                    throw new RuntimeException("只有草稿或已取消的任务可以发布");
                }
                task.setStatus(0); // 待接取
                break;
                
            case "save":
                // 保存草稿（发布前暂存）
                // 不检查状态，直接修改内容但不发布
                break;
                
            case "cancelPublish":
                // 发布者取消发布，回到草稿状态（而不是直接作废）
                if (task.getStatus() != 0) {
                    throw new RuntimeException("只有已发布且未接单的任务可以取消发布");
                }
                if (!task.getPublisherId().equals(operatorId)) {
                    throw new RuntimeException("只有任务发布者可以取消发布");
                }
                task.setStatus(-1); // 回到待发布（草稿）状态
                break;
                
            // ========== 跑腿员操作 ==========
            case "pickup":
                // 确认提货
                if (task.getStatus() != 1) {
                    throw new RuntimeException("只有已接单的任务可以提货");
                }
                if (!task.getRunnerId().equals(operatorId)) {
                    throw new RuntimeException("只有当前跑腿员可以提货");
                }
                task.setStatus(2); // 已提货
                task.setPickupTime(now);
                break;
                
            case "deliver":
                // 确认送达
                if (task.getStatus() != 2) {
                    throw new RuntimeException("只有已提货的任务可以送达");
                }
                if (!task.getRunnerId().equals(operatorId)) {
                    throw new RuntimeException("只有当前跑腿员可以确认送达");
                }
                task.setStatus(3); // 待确认 - 已送达，待发布者确认
                task.setDeliverTime(now);
                break;
                
            case "confirm":
                // 发布者确认收到
                if (task.getStatus() != 3) {
                    throw new RuntimeException("只有待确认状态的任务可以确认完成");
                }
                if (!task.getPublisherId().equals(operatorId)) {
                    throw new RuntimeException("只有任务发布者可以确认完成");
                }
                task.setStatus(4); // 已完成
                task.setCompleteTime(now);
                
                // 任务确认完成后，更新跑腿员统计数据
                User runner = userMapper.selectById(task.getRunnerId());
                if (runner != null) {
                    runner.setCompletedTasks(runner.getCompletedTasks() + 1);
                    runner.setTotalTasks(runner.getTotalTasks() + 1);
                    // 重新计算完成率
                    if (runner.getTotalTasks() > 0) {
                        runner.setCompletionRate(
                            new BigDecimal(runner.getCompletedTasks())
                                .divide(new BigDecimal(runner.getTotalTasks()), 4, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal("100"))
                        );
                    }
                    userMapper.updateById(runner);
                }
                
                task.setUpdateTime(now);
                baseMapper.updateById(task);
                
                // 发布者确认后才创建订单
                // 注意：直接传内存中的task对象，避免MyBatis缓存导致查询到旧状态
                try {
                    orderService.createOrderFromTask(task);
                } catch (Exception e) {
                    System.err.println("[严重] 订单创建失败，任务ID: " + taskId + ", 错误: " + e.getMessage());
                    task.setStatus(3);
                    task.setCompleteTime(null);
                    baseMapper.updateById(task);
                    throw new RuntimeException("订单创建失败，任务已回退到待确认状态", e);
                }
                
                // 返回最新的任务信息
                return baseMapper.selectById(taskId);
                
            case "return":
                // 跑腿员退回任务（已接单但未提货时可以退回）
                if (task.getStatus() != 1) {
                    throw new RuntimeException("只有已接单但未提货的任务可以退回");
                }
                if (!task.getRunnerId().equals(operatorId)) {
                    throw new RuntimeException("只有当前跑腿员可以退回任务");
                }
                if (StrUtil.isBlank(cancelReason)) {
                    throw new RuntimeException("请填写退回原因");
                }
                task.setStatus(6); // 已退回 - 退回任务大厅
                task.setRunnerId(null); // 清空跑腿员
                task.setAcceptTime(null); // 清空接单时间
                task.setCancelReason("跑腿员退回：" + cancelReason);
                break;
                
            default:
                throw new RuntimeException("无效的操作");
        }
        
        task.setUpdateTime(now);
        baseMapper.updateById(task);
        
        return task;
    }

    @Override
    @Transactional
    public Task updateTask(String taskId, String publisherId, String title, String description,
                          String pickupAddress, String deliveryAddress, BigDecimal reward, BigDecimal weight,
                          String expectCompleteTime, String contactName, String contactPhone, String remarks) {
        Task task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!task.getPublisherId().equals(publisherId)) {
            throw new RuntimeException("只有发布者可以修改任务");
        }
        // 草稿(-1)、已取消(5)、待接取(0)状态可以修改
        if (task.getStatus() != -1 && task.getStatus() != 0 && task.getStatus() != 5) {
            throw new RuntimeException("只有草稿、已取消或未接单的任务可以修改");
        }
        
        if (StrUtil.isNotBlank(title)) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (StrUtil.isNotBlank(pickupAddress)) {
            task.setPickupAddress(pickupAddress);
        }
        if (StrUtil.isNotBlank(deliveryAddress)) {
            task.setDeliveryAddress(deliveryAddress);
        }
        if (reward != null) {
            task.setReward(reward);
        }
        if (weight != null) {
            task.setWeight(weight);
        }
        if (StrUtil.isNotBlank(expectCompleteTime)) {
            task.setExpectCompleteTime(LocalDateTime.parse(expectCompleteTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (contactName != null) {
            task.setContactName(contactName);
        }
        if (contactPhone != null) {
            task.setContactPhone(contactPhone);
        }
        if (remarks != null) {
            task.setRemarks(remarks);
        }
        
        task.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(task);
        
        return task;
    }

    @Override
    public Page<Task> getMyTasks(String userId, String roleType, Integer status, Integer pageNum, Integer pageSize) {
        Page<Task> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        
        if ("publisher".equals(roleType)) {
            wrapper.eq(Task::getPublisherId, userId);
        } else if ("runner".equals(roleType)) {
            wrapper.eq(Task::getRunnerId, userId);
        }
        
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        
        wrapper.orderByDesc(Task::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Map<String, Object>> getRecommendTasks(BigDecimal longitude, BigDecimal latitude, Integer limit, String userId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getStatus, 0);
        wrapper.orderByDesc(Task::getCreateTime);
        wrapper.last("LIMIT " + limit);
        
        List<Task> tasks = baseMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Task task : tasks) {
            Map<String, Object> item = new HashMap<>();
            item.put("taskId", task.getId());
            item.put("taskNo", task.getTaskNo());
            item.put("title", task.getTitle());
            item.put("reward", task.getReward());
            item.put("status", task.getStatus());
            
            double distance = calculateDistance(latitude, longitude, task.getPickupLatitude(), task.getPickupLongitude());
            item.put("distance", (int) distance);
            item.put("distanceText", distance < 1000 ? (int) distance + "米" : String.format("%.1f", distance / 1000) + "公里");
            
            User publisher = userMapper.selectById(task.getPublisherId());
            if (publisher != null) {
                Map<String, Object> pubInfo = new HashMap<>();
                pubInfo.put("userId", publisher.getId());
                pubInfo.put("nickname", publisher.getNickname());
                pubInfo.put("creditScore", publisher.getCreditScore());
                item.put("publisher", pubInfo);
            }
            
            double matchScore = calculateMatchScore(distance, publisher != null ? publisher.getCreditScore() : 100, task.getReward());
            item.put("matchScore", matchScore);
            item.put("matchReason", generateMatchReason(distance, publisher != null ? publisher.getCreditScore() : 100));
            
            result.add(item);
        }
        
        result.sort((a, b) -> Double.compare((Double) b.get("matchScore"), (Double) a.get("matchScore")));
        
        return result;
    }
    
    private double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 999999;
        }
        
        double R = 6371000;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue())) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    private double calculateMatchScore(double distance, int creditScore, BigDecimal reward) {
        double distanceScore = Math.max(0, 100 - distance / 100);
        double creditScoreNorm = creditScore;
        double rewardScore = reward.multiply(new BigDecimal("10")).doubleValue();
        
        return distanceScore * 0.4 + creditScoreNorm * 0.3 + rewardScore * 0.2 + 10;
    }
    
    private String generateMatchReason(double distance, int creditScore) {
        List<String> reasons = new ArrayList<>();
        if (distance < 500) {
            reasons.add("距离近");
        }
        if (creditScore >= 90) {
            reasons.add("发布者信用高");
        }
        if (reasons.isEmpty()) {
            return "综合匹配";
        }
        return String.join("、", reasons);
    }
}
