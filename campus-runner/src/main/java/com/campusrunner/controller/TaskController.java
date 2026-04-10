package com.campusrunner.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.Task;
import com.campusrunner.service.TaskService;
import com.campusrunner.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/list")
    public Result<Page<Task>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Integer taskType,
            @RequestParam(required = false) BigDecimal minReward,
            @RequestParam(required = false) BigDecimal maxReward,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createTime") String sortField) {
        Page<Task> page = taskService.getTaskList(pageNum, pageSize, categoryId, taskType, minReward, maxReward, area, keyword, sortField);
        return Result.success(page);
    }

    @GetMapping("/recommend")
    public Result<List<Map<String, Object>>> recommend(
            @RequestParam BigDecimal longitude,
            @RequestParam BigDecimal latitude,
            @RequestParam(defaultValue = "5") Integer limit,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<Map<String, Object>> list = taskService.getRecommendTasks(longitude, latitude, limit, userId);
        return Result.success(list);
    }

    @GetMapping("/detail/{taskId}")
    public Result<Task> detail(@PathVariable String taskId) {
        Task task = taskService.getTaskDetail(taskId);
        if (task == null) {
            return Result.error(2005, "任务不存在");
        }
        return Result.success(task);
    }

    @PostMapping("/publish")
    public Result<Task> publish(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String publisherId = (String) request.getAttribute("userId");
            String categoryId = (String) params.get("categoryId");
            String title = (String) params.get("title");
            String description = (String) params.get("description");
            Integer taskType = params.get("taskType") != null ? (Integer) params.get("taskType") : 1;
            String pickupAddress = (String) params.get("pickupAddress");
            BigDecimal pickupLongitude = params.get("pickupLongitude") != null ? new BigDecimal(params.get("pickupLongitude").toString()) : null;
            BigDecimal pickupLatitude = params.get("pickupLatitude") != null ? new BigDecimal(params.get("pickupLatitude").toString()) : null;
            String deliveryAddress = (String) params.get("deliveryAddress");
            BigDecimal deliveryLongitude = params.get("deliveryLongitude") != null ? new BigDecimal(params.get("deliveryLongitude").toString()) : null;
            BigDecimal deliveryLatitude = params.get("deliveryLatitude") != null ? new BigDecimal(params.get("deliveryLatitude").toString()) : null;
            BigDecimal reward = new BigDecimal(params.get("reward").toString());
            BigDecimal weight = params.get("weight") != null ? new BigDecimal(params.get("weight").toString()) : BigDecimal.ZERO;
            String expectCompleteTime = (String) params.get("expectCompleteTime");
            String contactName = (String) params.get("contactName");
            String contactPhone = (String) params.get("contactPhone");
            String remarks = (String) params.get("remarks");

            Task task = taskService.publishTask(publisherId, categoryId, title, description, taskType,
                    pickupAddress, pickupLongitude, pickupLatitude, deliveryAddress, deliveryLongitude, deliveryLatitude,
                    reward, weight, expectCompleteTime, contactName, contactPhone, remarks);
            return Result.success("发布成功", task);
        } catch (RuntimeException e) {
            return Result.error(2002, e.getMessage());
        }
    }

    @PostMapping("/accept/{taskId}")
    public Result<Task> accept(@PathVariable String taskId, HttpServletRequest request) {
        try {
            String runnerId = (String) request.getAttribute("userId");
            Task task = taskService.acceptTask(taskId, runnerId);
            return Result.success("接单成功", task);
        } catch (RuntimeException e) {
            return Result.error(2007, e.getMessage());
        }
    }

    @PutMapping("/status/{taskId}")
    public Result<Task> updateStatus(@PathVariable String taskId, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String operatorId = (String) request.getAttribute("userId");
            String action = (String) params.get("action");
            String cancelReason = (String) params.get("cancelReason");
            Task task = taskService.updateTaskStatus(taskId, action, cancelReason, operatorId);
            return Result.success("状态更新成功", task);
        } catch (RuntimeException e) {
            return Result.error(2010, e.getMessage());
        }
    }

    @PutMapping("/update/{taskId}")
    public Result<Task> updateTask(@PathVariable String taskId, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String publisherId = (String) request.getAttribute("userId");
            String title = (String) params.get("title");
            String description = (String) params.get("description");
            String pickupAddress = (String) params.get("pickupAddress");
            String deliveryAddress = (String) params.get("deliveryAddress");
            BigDecimal reward = params.get("reward") != null ? new BigDecimal(params.get("reward").toString()) : null;
            BigDecimal weight = params.get("weight") != null ? new BigDecimal(params.get("weight").toString()) : null;
            String expectCompleteTime = (String) params.get("expectCompleteTime");
            String contactName = (String) params.get("contactName");
            String contactPhone = (String) params.get("contactPhone");
            String remarks = (String) params.get("remarks");

            Task task = taskService.updateTask(taskId, publisherId, title, description,
                    pickupAddress, deliveryAddress, reward, weight,
                    expectCompleteTime, contactName, contactPhone, remarks);
            return Result.success("修改成功", task);
        } catch (RuntimeException e) {
            return Result.error(2011, e.getMessage());
        }
    }

    @GetMapping("/my-list")
    public Result<Page<Task>> myList(
            @RequestParam String roleType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        Page<Task> page = taskService.getMyTasks(userId, roleType, status, pageNum, pageSize);
        return Result.success(page);
    }
}
