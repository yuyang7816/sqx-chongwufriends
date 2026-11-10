package com.sqx.app.pet.community.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "CommunityLikeVo")
public class CommunityLikeVo implements Serializable {
    private Long id;
    private Long postId;
    private Long userId;
    @Schema(description = "点赞用户昵称")
    private String userName;
    @Schema(description = "点赞用户头像")
    private String avatar;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
