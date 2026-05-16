package com.campusrunner.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("compensation_pool")
public class CompensationPool {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private BigDecimal balance;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private LocalDateTime updateTime;
}