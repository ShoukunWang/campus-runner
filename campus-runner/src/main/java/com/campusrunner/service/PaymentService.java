package com.campusrunner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusrunner.entity.Payment;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService extends IService<Payment> {

    Payment createPayment(String taskId, String payerId, String payeeId, BigDecimal amount, Integer paymentType);

    Payment pay(String paymentId);

    Payment refund(String paymentId, String reason);

    Page<Payment> getMyPayments(String userId, Integer status, Integer pageNum, Integer pageSize);

    Map<String, Object> getPaymentStatistics(String userId);
}
