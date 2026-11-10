package com.sqx.app.pet.adopt.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("adopt_visit")
public class AdoptVisit implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("application_id")
    private Long applicationId;
    @TableField("visit_time")
    private LocalDateTime visitTime;
    @TableField("content")
    private String content;
    @TableField("operator_id")
    private Long operatorId;
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
