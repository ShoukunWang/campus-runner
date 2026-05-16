package com.campusrunner.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.CreditRecord;
import com.campusrunner.entity.User;
import com.campusrunner.mapper.UserMapper;
import com.campusrunner.service.CreditService;
import com.campusrunner.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/credit")
public class CreditController {

    @Autowired
    private CreditService creditService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/my-records")
    public Result<Page<CreditRecord>> getMyCreditRecords(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = (String) request.getAttribute("userId");
        Page<CreditRecord> records = creditService.getCreditRecords(userId, pageNum, pageSize);
        return Result.success(records);
    }

    @GetMapping("/restore-price")
    public Result<Map<String, Object>> calculateRestorePrice(
            HttpServletRequest request,
            @RequestParam Integer targetScore) {
        String userId = (String) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        int currentScore = user.getCreditScore();
        BigDecimal price = creditService.calculateRestorePrice(currentScore, targetScore);

        Map<String, Object> result = new HashMap<>();
        result.put("currentScore", currentScore);
        result.put("targetScore", targetScore);
        result.put("price", price);

        return Result.success(result);
    }

    @PostMapping("/restore")
    public Result<Map<String, Object>> restoreCredit(
            HttpServletRequest request,
            @RequestBody Map<String, Object> requestBody) {
        String userId = (String) request.getAttribute("userId");
        Integer targetScore = (Integer) requestBody.get("targetScore");
        BigDecimal paidAmount = new BigDecimal(requestBody.get("paidAmount").toString());

        int newScore = creditService.restoreScore(userId, targetScore, paidAmount);

        Map<String, Object> result = new HashMap<>();
        result.put("newScore", newScore);
        result.put("paidAmount", paidAmount);

        return Result.success(result);
    }

    @GetMapping("/my-score")
    public Result<Map<String, Object>> getMyCreditScore(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("creditScore", user.getCreditScore());
        result.put("canPublish", creditService.canPublishTask(user.getCreditScore()));
        result.put("canAccept", creditService.canAcceptTask(user.getCreditScore()));
        result.put("isFrozen", creditService.isAccountFrozen(user.getCreditScore()));

        return Result.success(result);
    }
}