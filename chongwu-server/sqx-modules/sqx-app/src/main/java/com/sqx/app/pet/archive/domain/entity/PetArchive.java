package com.sqx.app.pet.archive.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("pet_archive")
@Schema(name = "PetArchive", description = "宠物档案")
public class PetArchive implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "门店ID")
    @TableField("store_id")
    private Long storeId;

    @Schema(description = "宠物名称")
    @TableField("name")
    private String name;

    @Schema(description = "宠物类型 dog/cat")
    @TableField("pet_type")
    private String petType;

    @Schema(description = "品种")
    @TableField("breed")
    private String breed;

    @Schema(description = "性别")
    @TableField("gender")
    private String gender;

    @Schema(description = "年龄描述")
    @TableField("age_desc")
    private String ageDesc;

    @Schema(description = "体重描述")
    @TableField("weight_desc")
    private String weightDesc;

    @Schema(description = "宠物编号")
    @TableField("pet_code")
    private String petCode;

    @Schema(description = "等级描述")
    @TableField("level_desc")
    private String levelDesc;

    @Schema(description = "状态标签")
    @TableField("status_label")
    private String statusLabel;

    @Schema(description = "描述")
    @TableField("description")
    private String description;

    @Schema(description = "擅长JSON")
    @TableField("good_at")
    private String goodAt;

    @Schema(description = "忌讳JSON")
    @TableField("avoid_items")
    private String avoidItems;

    @Schema(description = "标签JSON")
    @TableField("tags")
    private String tags;

    @Schema(description = "头像")
    @TableField("avatar_url")
    private String avatarUrl;

    @Schema(description = "点赞数")
    @TableField("like_count")
    private Integer likeCount;

    @Schema(description = "可领养 0否 1是")
    @TableField("adoptable")
    private Integer adoptable;

    @Schema(description = "审核状态 1通过 0待审 2拒绝")
    @TableField("audit_status")
    private Integer auditStatus;

    @Schema(description = "审核拒绝原因")
    @TableField("reject_reason")
    private String rejectReason;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

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
