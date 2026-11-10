package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "更新动态")
public class PostUpdateBo {
    @NotNull(message = "ID不能为空")
    private Long id;
    private Long storeId;
    private Long petId;
    @NotBlank(message = "内容不能为空")
    private String content;
    private String mediaUrls;
    private Integer mediaType;
    private Integer visibility;
    private Integer allowLike;
    private Integer allowComment;
}
