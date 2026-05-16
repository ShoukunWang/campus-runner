package com.campusrunner.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusrunner.entity.Admin;
import com.campusrunner.entity.Appeal;
import com.campusrunner.entity.CompensationPool;
import com.campusrunner.entity.Task;
import com.campusrunner.entity.User;
import com.campusrunner.mapper.AdminMapper;
import com.campusrunner.mapper.AppealMapper;
import com.campusrunner.mapper.CompensationPoolMapper;
import com.campusrunner.mapper.TaskMapper;
import com.campusrunner.mapper.UserMapper;
import com.campusrunner.service.AdminService;
import com.campusrunner.service.CreditService;
import com.campusrunner.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private AppealMapper appealMapper;

    @Autowired
    private CompensationPoolMapper compensationPoolMapper;

    @Autowired
    private CreditService creditService;

    @Override
    public Map<String, Object> login(String username, String password) {
        Admin admin = baseMapper.selectByUsername(username);

        if (admin == null) {
            throw new RuntimeException("账号或密码错误");
        }

        if (!DigestUtil.bcryptCheck(password, admin.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }

        if (admin.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        admin.setLastLoginTime(LocalDateTime.now());
        baseMapper.updateById(admin);

        String token = jwtUtil.generateToken(admin.getId(), admin.getUsername(), admin.getRole(), "ADMIN");

        Map<String, Object> result = new HashMap<>();
        result.put("adminId", admin.getId());
        result.put("username", admin.getUsername());
        result.put("realName", admin.getRealName());
        result.put("role", admin.getRole());
        result.put("roleName", admin.getRole() == 1 ? "超级管理员" : "普通管理员");
        result.put("token", token);
        result.put("expireTime", jwtUtil.getExpirationDateFromToken(token));
        return result;
    }

    @Override
    public Admin getCurrentAdmin(String adminId) {
        if (adminId == null) {
            return null;
        }
        Admin admin = baseMapper.selectById(adminId);
        if (admin != null) {
            admin.setPassword(null);
        }
        return admin;
    }

    @Override
    public Page<User> getUserList(Integer pageNum, Integer pageSize, String keyword, Integer role, Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }

        if (role != null) {
            wrapper.eq(User::getRole, role);
        }

        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public User updateUserStatus(String userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return user;
    }

    @Override
    @Transactional
    public User updateUserInfo(String userId, String nickname, String phone, String email, Integer creditScore) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (StrUtil.isNotBlank(nickname)) {
            user.setNickname(nickname);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (email != null) {
            user.setEmail(email);
        }
        if (creditScore != null) {
            user.setCreditScore(creditScore);
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        return user;
    }

    @Override
    public Page<Task> getTaskList(Integer pageNum, Integer pageSize, String taskNo, String title,
                                  Integer status, String publisherName, String runnerName) {
        Page<Task> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(taskNo)) {
            wrapper.like(Task::getTaskNo, taskNo);
        }

        if (StrUtil.isNotBlank(title)) {
            wrapper.like(Task::getTitle, title);
        }

        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }

        wrapper.orderByDesc(Task::getCreateTime);
        return taskMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public Task updateTask(String taskId, String title, String description, BigDecimal reward,
                          Integer status, String cancelReason) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (StrUtil.isNotBlank(title)) {
            task.setTitle(title);
        }
        if (StrUtil.isNotBlank(description)) {
            task.setDescription(description);
        }
        if (reward != null) {
            task.setReward(reward);
        }
        if (status != null) {
            if ((status == 5 || status == 6) && StrUtil.isBlank(cancelReason)) {
                throw new RuntimeException("取消或退回任务请填写原因");
            }
            task.setStatus(status);
            if (StrUtil.isNotBlank(cancelReason)) {
                task.setCancelReason(cancelReason);
            }
        }

        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        return task;
    }

    @Override
    @Transactional
    public void deleteTask(String taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        taskMapper.deleteById(taskId);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        long totalUsers = userMapper.selectCount(null);
        long totalTasks = taskMapper.selectCount(null);
        long totalCompletedTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getStatus, 4));
        long totalPendingTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getStatus, 0));
        long totalOngoingTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().and(w -> w.eq(Task::getStatus, 1).or().eq(Task::getStatus, 2)));
        long totalCancelledTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().and(w -> w.eq(Task::getStatus, 5).or().eq(Task::getStatus, 6)));
        long totalConfirmingTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getStatus, 3));

        // 计算总酬金
        List<Task> allTasks = taskMapper.selectList(null);
        BigDecimal totalReward = allTasks.stream()
                .map(Task::getReward)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

        long todayNewUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .ge(User::getCreateTime, todayStart)
                        .lt(User::getCreateTime, todayEnd));

        long todayNewTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>()
                        .ge(Task::getCreateTime, todayStart)
                        .lt(Task::getCreateTime, todayEnd));

        long todayCompletedTasks = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getStatus, 4)
                        .ge(Task::getCompleteTime, todayStart)
                        .lt(Task::getCompleteTime, todayEnd));

        result.put("totalUsers", totalUsers);
        result.put("totalTasks", totalTasks);
        result.put("totalCompletedTasks", totalCompletedTasks);
        result.put("totalPendingTasks", totalPendingTasks);
        result.put("totalOngoingTasks", totalOngoingTasks);
        result.put("totalCancelledTasks", totalCancelledTasks);
        result.put("totalReward", totalReward);

        // 前端期望的扁平键名
        result.put("pendingTasks", totalPendingTasks);
        result.put("runningTasks", totalOngoingTasks);
        result.put("confirmingTasks", totalConfirmingTasks);
        result.put("completedTasks", totalCompletedTasks);
        result.put("cancelledTasks", totalCancelledTasks);

        Map<String, Object> todayStats = new HashMap<>();
        todayStats.put("newUsers", todayNewUsers);
        todayStats.put("newTasks", todayNewTasks);
        todayStats.put("completedTasks", todayCompletedTasks);

        result.put("today", todayStats);

        return result;
    }

    @Override
    public Page<Admin> getAdminList(Integer pageNum, Integer pageSize, String keyword) {
        Page<Admin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Admin::getUsername, keyword)
                    .or().like(Admin::getRealName, keyword)
                    .or().like(Admin::getPhone, keyword));
        }

        wrapper.orderByDesc(Admin::getCreateTime);
        Page<Admin> result = baseMapper.selectPage(page, wrapper);
        // 清除密码
        result.getRecords().forEach(admin -> admin.setPassword(null));
        return result;
    }

    @Override
    @Transactional
    public Admin addAdmin(String username, String password, String realName, String phone, String email, Integer role, String createBy) {
        // 检查用户名是否已存在
        Admin existAdmin = baseMapper.selectByUsername(username);
        if (existAdmin != null) {
            throw new RuntimeException("用户名已存在");
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(DigestUtil.bcrypt(password));
        admin.setRealName(realName);
        admin.setPhone(phone);
        admin.setEmail(email);
        admin.setRole(role != null ? role : 0); // 默认普通管理员
        admin.setStatus(1); // 默认启用
        admin.setCreateBy(createBy);
        admin.setCreateTime(LocalDateTime.now());

        baseMapper.insert(admin);
        admin.setPassword(null);
        return admin;
    }

    @Override
    @Transactional
    public Admin updateAdmin(String adminId, String realName, String phone, String email, Integer role, Integer status) {
        Admin admin = baseMapper.selectById(adminId);
        if (admin == null) {
            throw new RuntimeException("管理员不存在");
        }

        if (StrUtil.isNotBlank(realName)) {
            admin.setRealName(realName);
        }
        if (phone != null) {
            admin.setPhone(phone);
        }
        if (email != null) {
            admin.setEmail(email);
        }
        if (role != null) {
            admin.setRole(role);
        }
        if (status != null) {
            admin.setStatus(status);
        }
        admin.setUpdateTime(LocalDateTime.now());

        baseMapper.updateById(admin);
        admin.setPassword(null);
        return admin;
    }

    @Override
    public Page<Appeal> getAppealList(Integer pageNum, Integer pageSize, Integer status) {
        Page<Appeal> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Appeal::getStatus, status);
        }
        
        wrapper.orderByDesc(Appeal::getCreateTime);
        return appealMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Appeal auditAppeal(String appealId, String adminId, Integer status, String remark) {
        Appeal appeal = appealMapper.selectById(appealId);
        if (appeal == null) {
            throw new RuntimeException("申诉不存在");
        }
        
        if (appeal.getStatus() != 0) {
            throw new RuntimeException("申诉已处理");
        }
        
        appeal.setStatus(status);
        appeal.setAdminId(adminId);
        appeal.setAuditRemark(remark);
        appeal.setUpdateTime(LocalDateTime.now());
        
        if (status == 1) {
            int deductAmount = 0;
            String reason = "";
            
            switch (appeal.getAppealType()) {
                case 1:
                    deductAmount = 15;
                    reason = "拒不支付";
                    break;
                case 2:
                    deductAmount = 20;
                    reason = "虚假送达";
                    break;
                case 3:
                    deductAmount = 10;
                    reason = "恶意退款";
                    break;
                case 4:
                    deductAmount = 20;
                    reason = "物品损坏/丢失";
                    break;
                case 5:
                    deductAmount = 15;
                    reason = "虚假确认";
                    break;
            }
            
            creditService.deductScore(appeal.getRespondentId(), deductAmount, reason, appeal.getOrderId(), appeal.getTaskId());
            
            BigDecimal compensationAmount = appeal.getCompensationAmount();
            if (compensationAmount != null && compensationAmount.compareTo(BigDecimal.ZERO) > 0) {
                creditService.deductFromCompensationPool(compensationAmount);
            }
        }
        
        appealMapper.updateById(appeal);
        return appeal;
    }

    @Override
    public Map<String, Object> getCompensationPool() {
        CompensationPool pool = compensationPoolMapper.selectById("1");
        Map<String, Object> result = new HashMap<>();
        
        if (pool != null) {
            result.put("balance", pool.getBalance());
            result.put("totalIncome", pool.getTotalIncome());
            result.put("totalExpense", pool.getTotalExpense());
        } else {
            result.put("balance", BigDecimal.ZERO);
            result.put("totalIncome", BigDecimal.ZERO);
            result.put("totalExpense", BigDecimal.ZERO);
        }
        
        return result;
    }
}
