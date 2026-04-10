package com.campusrunner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusrunner.entity.Payment;
import com.campusrunner.entity.Task;
import com.campusrunner.mapper.PaymentMapper;
import com.campusrunner.mapper.TaskMapper;
import com.campusrunner.service.PaymentService;
import com.campusrunner.utils.SnowflakeIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private SnowflakeIdUtil snowflakeIdUtil;

    @Override
    @Transactional
    public Payment createPayment(String taskId, String payerId, String payeeId, BigDecimal amount, Integer paymentType) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setTaskId(taskId);
        payment.setPayerId(payerId);
        payment.setPayeeId(payeeId);
        payment.setAmount(amount);
        payment.setPaymentType(paymentType != null ? paymentType : 1);
        payment.setStatus(0);
        payment.setCreateTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(payment);
        return payment;
    }

    private String generatePaymentNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
               String.format("%04d", (int)(Math.random() * 10000));
    }

    @Override
    @Transactional
    public Payment pay(String paymentId) {
        Payment payment = baseMapper.selectById(paymentId);
        if (payment == null) {
            throw new RuntimeException("支付记录不存在");
        }

        if (payment.getStatus() != 0) {
            throw new RuntimeException("该订单已支付或已退款");
        }

        payment.setStatus(1);
        payment.setPayTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());

        baseMapper.updateById(payment);
        return payment;
    }

    @Override
    @Transactional
    public Payment refund(String paymentId, String reason) {
        Payment payment = baseMapper.selectById(paymentId);
        if (payment == null) {
            throw new RuntimeException("支付记录不存在");
        }

        if (payment.getStatus() != 1) {
            throw new RuntimeException("该订单未支付或已退款");
        }

        payment.setStatus(2);
        payment.setRefundTime(LocalDateTime.now());
        payment.setRefundReason(reason);
        payment.setUpdateTime(LocalDateTime.now());

        baseMapper.updateById(payment);
        return payment;
    }

    @Override
    public Page<Payment> getMyPayments(String userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Payment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.and(w -> w.eq(Payment::getPayerId, userId).or().eq(Payment::getPayeeId, userId));
        
        if (status != null) {
            wrapper.eq(Payment::getStatus, status);
        }
        
        wrapper.orderByDesc(Payment::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> getPaymentStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<Payment> paidWrapper = new LambdaQueryWrapper<>();
        paidWrapper.eq(Payment::getPayeeId, userId);
        paidWrapper.eq(Payment::getStatus, 1);
        long totalIncome = baseMapper.selectCount(paidWrapper);
        stats.put("totalIncomeCount", totalIncome);

        LambdaQueryWrapper<Payment> paidAmountWrapper = new LambdaQueryWrapper<>();
        paidAmountWrapper.eq(Payment::getPayeeId, userId);
        paidAmountWrapper.eq(Payment::getStatus, 1);
        stats.put("totalIncomeAmount", calculateTotalAmount(paidAmountWrapper));

        LambdaQueryWrapper<Payment> expenseWrapper = new LambdaQueryWrapper<>();
        expenseWrapper.eq(Payment::getPayerId, userId);
        expenseWrapper.eq(Payment::getStatus, 1);
        long totalExpense = baseMapper.selectCount(expenseWrapper);
        stats.put("totalExpenseCount", totalExpense);

        LambdaQueryWrapper<Payment> expenseAmountWrapper = new LambdaQueryWrapper<>();
        expenseAmountWrapper.eq(Payment::getPayerId, userId);
        expenseAmountWrapper.eq(Payment::getStatus, 1);
        stats.put("totalExpenseAmount", calculateTotalAmount(expenseAmountWrapper));

        return stats;
    }

    private BigDecimal calculateTotalAmount(LambdaQueryWrapper<Payment> wrapper) {
        return baseMapper.selectList(wrapper).stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
