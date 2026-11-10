package com.sqx.app.pet.strategy.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("pet_strategy")
@Schema(name = "PetStrategy", description = "养宠攻略")
public class PetStrategy implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("category_id")
    private Long categoryId;
    @TableField("title")
    private String title;
    @TableField("sub_title")
    private String subTitle;
    @TableField("tag")
    private String tag;
    @TableField("cover_url")
    private String coverUrl;
    @TableField("content")
    private String content;
    @TableField("view_count")
    private Integer viewCount;
    @TableField("like_count")
    private Integer likeCount;
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
