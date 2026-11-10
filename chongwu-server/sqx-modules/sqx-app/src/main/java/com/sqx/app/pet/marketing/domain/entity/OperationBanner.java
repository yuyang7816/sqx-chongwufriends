package com.sqx.app.pet.marketing.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("operation_banner")
@Schema(name = "OperationBanner", description = "运营位")
public class OperationBanner implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @Schema(description = "副标题")
    @TableField("sub_title")
    private String subTitle;

    @TableField("image_url")
    private String imageUrl;

    @TableField("jump_url")
    private String jumpUrl;

    @TableField("position_code")
    private String positionCode;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private Integer status;

    @Schema(description = "点击次数")
    @TableField("click_count")
    private Integer clickCount;

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
