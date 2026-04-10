package com.campusrunner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_category")
public class TaskCategory {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String name;

    private String parentId;

    private Integer sortOrder;

    private String icon;

    private Integer status;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
