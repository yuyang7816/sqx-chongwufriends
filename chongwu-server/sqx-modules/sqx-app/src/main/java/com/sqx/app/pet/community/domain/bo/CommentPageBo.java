package com.sqx.app.pet.community.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "评论分页")
public class CommentPageBo extends PageParam {
    @NotNull(message = "动态ID不能为空")
    private Long postId;
}
