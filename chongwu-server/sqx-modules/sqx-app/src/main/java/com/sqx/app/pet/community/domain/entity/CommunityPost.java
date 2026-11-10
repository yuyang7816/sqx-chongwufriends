package com.sqx.app.pet.community.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("community_post")
@Schema(name = "CommunityPost", description = "社区动态")
public class CommunityPost implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("store_id")
    private Long storeId;

    @TableField("pet_id")
    private Long petId;

    @TableField("user_name")
    private String userName;

    @TableField("content")
    private String content;

    @TableField("media_urls")
    private String mediaUrls;

    @TableField("media_type")
    private Integer mediaType;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("comment_count")
    private Integer commentCount;

    @TableField("share_count")
    private Integer shareCount;

    @TableField("visibility")
    private Integer visibility;

    @TableField("allow_like")
    private Integer allowLike;

    @TableField("allow_comment")
    private Integer allowComment;

    @TableField("audit_status")
    private Integer auditStatus;

    @TableField("reject_reason")
    private String rejectReason;

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
