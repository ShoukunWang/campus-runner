package com.campusrunner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String taskId;

    private String reviewerId;

    private String revieweeId;

    private Integer reviewType;

    private Integer rating;

    private String content;

    private String tags;

    private Integer isAnonymous;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
