package com.campusrunner.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.CompensationPool;
import com.campusrunner.entity.CreditRecord;
import com.campusrunner.entity.User;
import com.campusrunner.mapper.CompensationPoolMapper;
import com.campusrunner.mapper.CreditRecordMapper;
import com.campusrunner.mapper.UserMapper;
import com.campusrunner.service.CreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CreditServiceImpl implements CreditService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CreditRecordMapper creditRecordMapper;

    @Autowired
    private CompensationPoolMapper compensationPoolMapper;

    private static final int MAX_SCORE = 100;
    private static final int PUBLISH_THRESHOLD = 40;
    private static final int ACCEPT_THRESHOLD = 20;
    private static final int FREEZE_THRESHOLD = 20;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deductScore(String userId, int amount, String reason, String orderId, String taskId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        int beforeScore = user.getCreditScore();
        int afterScore = Math.max(0, beforeScore - amount);

        user.setCreditScore(afterScore);
        userMapper.updateById(user);

        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setChangeAmount(-amount);
        record.setBeforeScore(beforeScore);
        record.setAfterScore(afterScore);
        record.setType(1);
        record.setReason(reason);
        record.setRelatedOrderId(orderId);
        record.setRelatedTaskId(taskId);
        record.setCreateTime(LocalDateTime.now());
        creditRecordMapper.insert(record);

        if (afterScore < FREEZE_THRESHOLD && user.getStatus() == 1) {
            user.setStatus(0);
            userMapper.updateById(user);
        }

        return afterScore;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int restoreScore(String userId, int targetScore, BigDecimal paidAmount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        int currentScore = user.getCreditScore();
        if (targetScore <= currentScore) {
            throw new RuntimeException("目标分数不能低于当前分数");
        }
        if (targetScore > MAX_SCORE) {
            targetScore = MAX_SCORE;
        }

        BigDecimal expectedAmount = calculateRestorePrice(currentScore, targetScore);
        if (paidAmount.compareTo(expectedAmount) < 0) {
            throw new RuntimeException("支付金额不足");
        }

        int beforeScore = currentScore;
        int afterScore = targetScore;

        user.setCreditScore(afterScore);
        userMapper.updateById(user);

        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setChangeAmount(afterScore - beforeScore);
        record.setBeforeScore(beforeScore);
        record.setAfterScore(afterScore);
        record.setType(2);
        record.setReason("支付恢复信誉分");
        record.setCreateTime(LocalDateTime.now());
        creditRecordMapper.insert(record);

        addToCompensationPool(paidAmount);

        if (user.getStatus() == 0 && afterScore >= FREEZE_THRESHOLD) {
            user.setStatus(1);
            userMapper.updateById(user);
        }

        return afterScore;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int naturalRestore(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        int beforeScore = user.getCreditScore();
        int afterScore = Math.min(MAX_SCORE, beforeScore + 5);

        if (beforeScore >= MAX_SCORE) {
            return beforeScore;
        }

        user.setCreditScore(afterScore);
        userMapper.updateById(user);

        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setChangeAmount(5);
        record.setBeforeScore(beforeScore);
        record.setAfterScore(afterScore);
        record.setType(3);
        record.setReason("连续30天无违规自动恢复");
        record.setCreateTime(LocalDateTime.now());
        creditRecordMapper.insert(record);

        return afterScore;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int adjustScore(String userId, int amount, String reason) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        int beforeScore = user.getCreditScore();
        int afterScore = Math.max(0, Math.min(MAX_SCORE, beforeScore + amount));

        user.setCreditScore(afterScore);
        userMapper.updateById(user);

        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setChangeAmount(amount);
        record.setBeforeScore(beforeScore);
        record.setAfterScore(afterScore);
        record.setType(4);
        record.setReason(reason);
        record.setCreateTime(LocalDateTime.now());
        creditRecordMapper.insert(record);

        return afterScore;
    }

    @Override
    public Page<CreditRecord> getCreditRecords(String userId, int pageNum, int pageSize) {
        Page<CreditRecord> page = new Page<>(pageNum, pageSize);
        return creditRecordMapper.selectByUserId(page, userId);
    }

    @Override
    public BigDecimal calculateRestorePrice(int currentScore, int targetScore) {
        if (targetScore <= currentScore) {
            return BigDecimal.ZERO;
        }
        if (targetScore > MAX_SCORE) {
            targetScore = MAX_SCORE;
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        if (currentScore < 80 && targetScore > currentScore) {
            int lower = Math.max(currentScore, 60);
            int upper = Math.min(targetScore, 79);
            if (lower <= upper) {
                totalPrice = totalPrice.add(BigDecimal.valueOf(upper - lower + 1).multiply(new BigDecimal("2")));
            }
        }

        if (currentScore < 60 && targetScore > currentScore) {
            int lower = Math.max(currentScore, 40);
            int upper = Math.min(targetScore, 59);
            if (lower <= upper) {
                totalPrice = totalPrice.add(BigDecimal.valueOf(upper - lower + 1).multiply(new BigDecimal("5")));
            }
        }

        if (currentScore < 40 && targetScore > currentScore) {
            int lower = Math.max(currentScore, 20);
            int upper = Math.min(targetScore, 39);
            if (lower <= upper) {
                totalPrice = totalPrice.add(BigDecimal.valueOf(upper - lower + 1).multiply(new BigDecimal("10")));
            }
        }

        if (currentScore < 20 && targetScore > currentScore) {
            int upper = Math.min(targetScore, 19);
            if (currentScore <= upper) {
                totalPrice = totalPrice.add(BigDecimal.valueOf(upper - currentScore + 1).multiply(new BigDecimal("20")));
            }
        }

        if (currentScore >= 80 && targetScore > currentScore) {
            int upper = Math.min(targetScore, 100);
            if (currentScore <= upper) {
                totalPrice = totalPrice.add(BigDecimal.valueOf(upper - currentScore + 1).multiply(new BigDecimal("0.5")));
            }
        }

        return totalPrice;
    }

    @Override
    public boolean canPublishTask(int creditScore) {
        return creditScore >= PUBLISH_THRESHOLD;
    }

    @Override
    public boolean canAcceptTask(int creditScore) {
        return creditScore >= ACCEPT_THRESHOLD;
    }

    @Override
    public boolean isAccountFrozen(int creditScore) {
        return creditScore < FREEZE_THRESHOLD;
    }

    @Override
    public BigDecimal getCompensationPoolBalance() {
        CompensationPool pool = compensationPoolMapper.selectById("1");
        return pool != null ? pool.getBalance() : BigDecimal.ZERO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addToCompensationPool(BigDecimal amount) {
        CompensationPool pool = compensationPoolMapper.selectById("1");
        if (pool == null) {
            pool = new CompensationPool();
            pool.setId("1");
            pool.setBalance(amount);
            pool.setTotalIncome(amount);
            pool.setTotalExpense(BigDecimal.ZERO);
            pool.setUpdateTime(LocalDateTime.now());
            compensationPoolMapper.insert(pool);
        } else {
            pool.setBalance(pool.getBalance().add(amount));
            pool.setTotalIncome(pool.getTotalIncome().add(amount));
            pool.setUpdateTime(LocalDateTime.now());
            compensationPoolMapper.updateById(pool);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductFromCompensationPool(BigDecimal amount) {
        CompensationPool pool = compensationPoolMapper.selectById("1");
        
        if (pool == null) {
            throw new RuntimeException("赔偿金池余额不足");
        }
        
        if (pool.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("赔偿金池余额不足");
        }
        
        pool.setBalance(pool.getBalance().subtract(amount));
        pool.setTotalExpense(pool.getTotalExpense().add(amount));
        pool.setUpdateTime(LocalDateTime.now());
        compensationPoolMapper.updateById(pool);
        
        return true;
    }
}