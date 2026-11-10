package com.sqx.app.pet.community.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("community_report")
public class CommunityReport implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("target_type")
    private Integer targetType;
    @TableField("target_id")
    private Long targetId;
    @TableField("user_id")
    private Long userId;
    @TableField("reason")
    private String reason;
    @TableField("status")
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
