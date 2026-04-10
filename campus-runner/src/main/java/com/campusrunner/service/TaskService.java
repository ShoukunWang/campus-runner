package com.campusrunner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusrunner.entity.Task;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TaskService extends IService<Task> {

    Page<Task> getTaskList(Integer pageNum, Integer pageSize, String categoryId, Integer taskType,
                          BigDecimal minReward, BigDecimal maxReward, String area, String sortField);

    Task getTaskDetail(String taskId);

    Task publishTask(String publisherId, String categoryId, String title, String description,
                    Integer taskType, String pickupAddress, BigDecimal pickupLongitude, BigDecimal pickupLatitude,
                    String deliveryAddress, BigDecimal deliveryLongitude, BigDecimal deliveryLatitude,
                    BigDecimal reward, BigDecimal weight, String expectCompleteTime,
                    String contactName, String contactPhone, String remarks);

    Task acceptTask(String taskId, String runnerId);

    Task updateTaskStatus(String taskId, String action, String cancelReason, String operatorId);

    Task updateTask(String taskId, String publisherId, String title, String description,
                    String pickupAddress, String deliveryAddress, BigDecimal reward, BigDecimal weight,
                    String expectCompleteTime, String contactName, String contactPhone, String remarks);

    Page<Task> getMyTasks(String userId, String roleType, Integer status, Integer pageNum, Integer pageSize);

    List<Map<String, Object>> getRecommendTasks(BigDecimal longitude, BigDecimal latitude, Integer limit, String userId);
}
