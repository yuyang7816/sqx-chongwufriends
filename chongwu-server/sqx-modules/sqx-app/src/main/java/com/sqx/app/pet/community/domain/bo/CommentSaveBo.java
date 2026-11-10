package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "保存评论")
public class CommentSaveBo {
    @NotNull(message = "动态ID不能为空")
    private Long postId;
    private String userName;
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
