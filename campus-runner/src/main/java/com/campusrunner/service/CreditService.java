package com.campusrunner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusrunner.entity.CreditRecord;

import java.math.BigDecimal;

public interface CreditService {

    int deductScore(String userId, int amount, String reason, String orderId, String taskId);

    int restoreScore(String userId, int targetScore, BigDecimal paidAmount);

    int naturalRestore(String userId);

    int adjustScore(String userId, int amount, String reason);

    Page<CreditRecord> getCreditRecords(String userId, int pageNum, int pageSize);

    BigDecimal calculateRestorePrice(int currentScore, int targetScore);

    boolean canPublishTask(int creditScore);

    boolean canAcceptTask(int creditScore);

    boolean isAccountFrozen(int creditScore);

    BigDecimal getCompensationPoolBalance();

    boolean addToCompensationPool(BigDecimal amount);

    boolean deductFromCompensationPool(BigDecimal amount);
}