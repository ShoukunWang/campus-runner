package com.campusrunner.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.Appeal;
import com.campusrunner.entity.Order;
import com.campusrunner.entity.Task;
import com.campusrunner.mapper.AppealMapper;
import com.campusrunner.mapper.OrderMapper;
import com.campusrunner.mapper.TaskMapper;
import com.campusrunner.mapper.UserMapper;
import com.campusrunner.service.CreditService;
import com.campusrunner.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appeal")
public class AppealController {

    @Autowired
    private AppealMapper appealMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CreditService creditService;

    @PostMapping
    public Result<Appeal> createAppeal(
            HttpServletRequest request,
            @RequestBody Map<String, Object> requestBody) {
        String userId = (String) request.getAttribute("userId");
        String orderId = (String) requestBody.get("orderId");
        Integer appealType = (Integer) requestBody.get("appealType");
        String evidence = (String) requestBody.get("evidence");

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        Task task = taskMapper.selectById(order.getTaskId());
        if (task == null) {
            return Result.error("任务不存在");
        }

        String respondentId;
        BigDecimal compensationAmount = BigDecimal.ZERO;

        switch (appealType) {
            case 1:
                respondentId = order.getPublisherId();
                compensationAmount = order.getAmount();
                break;
            case 2:
                respondentId = order.getRunnerId();
                compensationAmount = order.getAmount();
                break;
            case 3:
                respondentId = order.getPublisherId();
                compensationAmount = order.getAmount();
                break;
            case 4:
                respondentId = order.getRunnerId();
                compensationAmount = order.getAmount();
                break;
            case 5:
                respondentId = order.getPublisherId();
                compensationAmount = order.getAmount();
                break;
            default:
                return Result.error("无效的申诉类型");
        }

        Appeal appeal = new Appeal();
        appeal.setOrderId(orderId);
        appeal.setTaskId(order.getTaskId());
        appeal.setApplicantId(userId);
        appeal.setRespondentId(respondentId);
        appeal.setAppealType(appealType);
        appeal.setStatus(0);
        appeal.setCompensationAmount(compensationAmount);
        appeal.setEvidence(evidence);
        appeal.setCreateTime(LocalDateTime.now());
        appeal.setUpdateTime(LocalDateTime.now());

        appealMapper.insert(appeal);

        return Result.success(appeal);
    }

    @GetMapping("/my")
    public Result<Page<Appeal>> getMyAppeals(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = (String) request.getAttribute("userId");
        Page<Appeal> page = new Page<>(pageNum, pageSize);
        Page<Appeal> appeals = appealMapper.selectByApplicantId(page, userId);
        return Result.success(appeals);
    }

    @GetMapping("/{id}")
    public Result<Appeal> getAppealDetail(@PathVariable String id) {
        Appeal appeal = appealMapper.selectById(id);
        if (appeal == null) {
            return Result.error("申诉不存在");
        }
        return Result.success(appeal);
    }
}