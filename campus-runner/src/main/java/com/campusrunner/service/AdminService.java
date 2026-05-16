package com.campusrunner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusrunner.entity.Admin;
import com.campusrunner.entity.Appeal;
import com.campusrunner.entity.Task;
import com.campusrunner.entity.User;

import java.math.BigDecimal;
import java.util.Map;

public interface AdminService extends IService<Admin> {

    Map<String, Object> login(String username, String password);

    Admin getCurrentAdmin(String adminId);

    Page<User> getUserList(Integer pageNum, Integer pageSize, String keyword, Integer role, Integer status);

    User updateUserStatus(String userId, Integer status);

    User updateUserInfo(String userId, String nickname, String phone, String email, Integer creditScore);

    Page<Task> getTaskList(Integer pageNum, Integer pageSize, String taskNo, String title, 
                          Integer status, String publisherName, String runnerName);

    Task updateTask(String taskId, String title, String description, BigDecimal reward, 
                   Integer status, String cancelReason);

    void deleteTask(String taskId);

    Map<String, Object> getStatistics();

    Page<Admin> getAdminList(Integer pageNum, Integer pageSize, String keyword);

    Admin addAdmin(String username, String password, String realName, String phone, String email, Integer role, String createBy);

    Admin updateAdmin(String adminId, String realName, String phone, String email, Integer role, Integer status);

    Page<Appeal> getAppealList(Integer pageNum, Integer pageSize, Integer status);

    Appeal auditAppeal(String appealId, String adminId, Integer status, String remark);

    Map<String, Object> getCompensationPool();
}
