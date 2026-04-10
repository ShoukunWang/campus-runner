package com.campusrunner.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.Payment;
import com.campusrunner.service.PaymentService;
import com.campusrunner.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    public Result<Payment> createPayment(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String payerId = (String) request.getAttribute("userId");
            String taskId = (String) params.get("taskId");
            String payeeId = (String) params.get("payeeId");
            BigDecimal amount = new BigDecimal(params.get("amount").toString());
            Integer paymentType = (Integer) params.get("paymentType");

            if (taskId == null || payeeId == null || amount == null) {
                return Result.error(400, "缺少必要参数");
            }

            Payment payment = paymentService.createPayment(taskId, payerId, payeeId, amount, paymentType);
            return Result.success("创建支付订单成功", payment);
        } catch (RuntimeException e) {
            return Result.error(6001, e.getMessage());
        }
    }

    @PostMapping("/pay/{paymentId}")
    public Result<Payment> pay(@PathVariable String paymentId, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            Payment payment = paymentService.pay(paymentId);
            return Result.success("支付成功", payment);
        } catch (RuntimeException e) {
            return Result.error(6002, e.getMessage());
        }
    }

    @PostMapping("/refund/{paymentId}")
    public Result<Payment> refund(@PathVariable String paymentId, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            String reason = (String) params.get("reason");
            Payment payment = paymentService.refund(paymentId, reason);
            return Result.success("退款成功", payment);
        } catch (RuntimeException e) {
            return Result.error(6003, e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<Page<Payment>> getMyPayments(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        Page<Payment> page = paymentService.getMyPayments(userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getPaymentStatistics(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        Map<String, Object> stats = paymentService.getPaymentStatistics(userId);
        return Result.success(stats);
    }
}
