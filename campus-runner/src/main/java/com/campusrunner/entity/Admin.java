package com.campusrunner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin")
public class Admin {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String username;

    private String password;

    private String realName;

    private String phone;

    private String email;

    private Integer role;

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
