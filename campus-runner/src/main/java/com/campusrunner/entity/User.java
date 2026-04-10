package com.campusrunner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String email;

    private String avatar;

    private Integer role;

    private Integer creditScore;

    private BigDecimal completionRate;

    private Integer totalTasks;

    private Integer completedTasks;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
