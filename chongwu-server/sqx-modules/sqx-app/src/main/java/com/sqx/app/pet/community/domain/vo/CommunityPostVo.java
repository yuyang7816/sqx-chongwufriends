package com.sqx.app.pet.community.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "CommunityPostVo")
public class CommunityPostVo implements Serializable {
    private Long id;
    private Long userId;
    private Long storeId;
    private Long petId;
    private String userName;
    @Schema(description = "用户头像")
    private String avatar;
    @Schema(description = "宠物名称")
    private String petName;
    @Schema(description = "门店名称")
    private String storeName;
    @Schema(description = "相对时间文案")
    private String timeText;
    private String content;
    private String mediaUrls;
    private Integer mediaType;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer visibility;
    private Integer allowLike;
    private Integer allowComment;
    private Integer auditStatus;
    @Schema(description = "审核拒绝原因")
    private String rejectReason;
    private Integer status;
    private Boolean liked;
    @Schema(description = "审核状态文案")
    private String auditStatusText;
    @Schema(description = "状态文案")
    private String statusText;
    @Schema(description = "可见范围文案")
    private String visibilityText;
    @Schema(description = "是否可编辑")
    private Boolean canEdit;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
