package com.sqx.app.pet.community.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "CommunityCommentVo")
public class CommunityCommentVo implements Serializable {
    private Long id;
    private Long postId;
    private Long userId;
    @Schema(description = "评论者昵称")
    private String userName;
    @Schema(description = "评论者头像")
    private String avatar;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
