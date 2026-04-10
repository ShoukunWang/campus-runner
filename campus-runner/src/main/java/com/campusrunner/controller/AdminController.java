package com.campusrunner.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.Admin;
import com.campusrunner.entity.Task;
import com.campusrunner.entity.User;
import com.campusrunner.service.AdminService;
import com.campusrunner.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> params) {
        try {
            String username = (String) params.get("username");
            String password = (String) params.get("password");

            if (username == null || password == null) {
                return Result.error(400, "缺少必要参数");
            }

            Map<String, Object> result = adminService.login(username, password);
            return Result.success("登录成功", result);
        } catch (RuntimeException e) {
            return Result.error(4001, e.getMessage());
        }
    }

    @GetMapping("/user/list")
    public Result<Page<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {

        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        Page<User> page = adminService.getUserList(pageNum, pageSize, keyword, role, status);
        page.getRecords().forEach(user -> user.setPassword(null));
        return Result.success(page);
    }

    @PutMapping("/user/status/{userId}")
    public Result<User> updateUserStatus(
            @PathVariable String userId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {

        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        try {
            Integer status = (Integer) params.get("status");
            if (status == null) {
                return Result.error(400, "状态不能为空");
            }

            User user = adminService.updateUserStatus(userId, status);
            user.setPassword(null);
            return Result.success("操作成功", user);
        } catch (RuntimeException e) {
            return Result.error(4002, e.getMessage());
        }
    }

    @PutMapping("/user/{userId}")
    public Result<User> updateUserInfo(
            @PathVariable String userId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {

        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        try {
            String nickname = (String) params.get("nickname");
            String phone = (String) params.get("phone");
            String email = (String) params.get("email");
            Integer creditScore = params.get("creditScore") != null ? 
                    Integer.parseInt(params.get("creditScore").toString()) : null;

            User user = adminService.updateUserInfo(userId, nickname, phone, email, creditScore);
            user.setPassword(null);
            return Result.success("修改成功", user);
        } catch (RuntimeException e) {
            return Result.error(4005, e.getMessage());
        }
    }

    @GetMapping("/task/list")
    public Result<Page<Task>> getTaskList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String taskNo,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String publisherName,
            @RequestParam(required = false) String runnerName,
            HttpServletRequest request) {

        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        Page<Task> page = adminService.getTaskList(pageNum, pageSize, taskNo, title, status, publisherName, runnerName);
        return Result.success(page);
    }

    @PutMapping("/task/{taskId}")
    public Result<Task> updateTask(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {

        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        try {
            String title = (String) params.get("title");
            String description = (String) params.get("description");
            BigDecimal reward = params.get("reward") != null ? new BigDecimal(params.get("reward").toString()) : null;
            Integer status = (Integer) params.get("status");
            String cancelReason = (String) params.get("cancelReason");

            Task task = adminService.updateTask(taskId, title, description, reward, status, cancelReason);
            return Result.success("修改成功", task);
        } catch (RuntimeException e) {
            return Result.error(4003, e.getMessage());
        }
    }

    @DeleteMapping("/task/{taskId}")
    public Result<Void> deleteTask(
            @PathVariable String taskId,
            HttpServletRequest request) {

        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        try {
            adminService.deleteTask(taskId);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(4004, e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        Map<String, Object> statistics = adminService.getStatistics();
        return Result.success(statistics);
    }

    @GetMapping("/current")
    public Result<Admin> getCurrentAdmin(HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        String adminId = (String) request.getAttribute("userId");
        Admin admin = adminService.getCurrentAdmin(adminId);
        if (admin == null) {
            return Result.error(404, "管理员信息不存在");
        }
        return Result.success(admin);
    }

    // ==================== 管理员管理 ====================

    @GetMapping("/list")
    public Result<Page<Admin>> getAdminList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {

        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        Page<Admin> page = adminService.getAdminList(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @PostMapping("/add")
    public Result<Admin> addAdmin(
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {

        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        // 只有超级管理员(role=1)才能添加新管理员
        Integer currentRole = (Integer) request.getAttribute("role");
        if (currentRole == null || currentRole != 1) {
            return Result.error(403, "只有超级管理员才能添加新管理员");
        }

        try {
            String username = (String) params.get("username");
            String password = (String) params.get("password");
            String realName = (String) params.get("realName");
            String phone = (String) params.get("phone");
            String email = (String) params.get("email");
            Integer role = params.get("role") != null ? 
                    Integer.parseInt(params.get("role").toString()) : 0;

            if (username == null || password == null) {
                return Result.error(400, "用户名和密码不能为空");
            }

            String createBy = (String) request.getAttribute("userId");
            Admin admin = adminService.addAdmin(username, password, realName, phone, email, role, createBy);
            return Result.success("添加成功", admin);
        } catch (RuntimeException e) {
            return Result.error(4006, e.getMessage());
        }
    }

    @PutMapping("/{adminId}")
    public Result<Admin> updateAdmin(
            @PathVariable String adminId,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {

        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        try {
            String realName = (String) params.get("realName");
            String phone = (String) params.get("phone");
            String email = (String) params.get("email");
            Integer role = params.get("role") != null ? 
                    Integer.parseInt(params.get("role").toString()) : null;
            Integer status = params.get("status") != null ? 
                    Integer.parseInt(params.get("status").toString()) : null;

            Admin admin = adminService.updateAdmin(adminId, realName, phone, email, role, status);
            return Result.success("修改成功", admin);
        } catch (RuntimeException e) {
            return Result.error(4007, e.getMessage());
        }
    }
}
