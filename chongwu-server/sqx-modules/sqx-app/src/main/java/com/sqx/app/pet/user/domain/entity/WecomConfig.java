package com.sqx.app.pet.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("wecom_config")
public class WecomConfig implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("qr_url")
    private String qrUrl;
    @TableField("wecom_id")
    private String wecomId;
    @TableField("xhs_title")
    private String xhsTitle;
    @TableField("xhs_subtitle")
    private String xhsSubtitle;
    @TableField("xhs_jump_url")
    private String xhsJumpUrl;
    @TableField("community_title")
    private String communityTitle;
    @TableField("community_subtitle")
    private String communitySubtitle;
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
