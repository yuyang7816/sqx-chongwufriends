package com.sqx.app.pet.island.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("island_pet")
@Schema(name = "IslandPet", description = "岛宅宠物状态")
public class IslandPet implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("hunger")
    private Integer hunger;

    @TableField("clean")
    private Integer clean;

    @TableField("mood")
    private Integer mood;

    @TableField("vitality")
    private Integer vitality;

    @TableField("intimacy")
    private Integer intimacy;

    @TableField("daily_progress")
    private Integer dailyProgress;

    @TableField("stat_date")
    private java.time.LocalDate statDate;

    @TableField("food_bowl_filled")
    private Integer foodBowlFilled;

    @TableField("water_bowl_filled")
    private Integer waterBowlFilled;

    @TableField("resident_pet_id")
    private Long residentPetId;

    @TableField("resident_pet_ids")
    private String residentPetIds;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("feed_cooldown_until")
    private LocalDateTime feedCooldownUntil;

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
