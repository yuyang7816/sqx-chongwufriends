package com.sqx.app.pet.island.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("island_task")
public class IslandTask implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("title")
    private String title;
    @TableField("description")
    private String description;
    @TableField("reward_desc")
    private String rewardDesc;
    @TableField("required_level")
    private Integer requiredLevel;
    @TableField("task_group")
    private String taskGroup;
    @TableField("action_type")
    private String actionType;
    @TableField("reward_coins")
    private Integer rewardCoins;
    @TableField("reward_points")
    private Integer rewardPoints;
    @TableField("sort")
    private Integer sort;
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
