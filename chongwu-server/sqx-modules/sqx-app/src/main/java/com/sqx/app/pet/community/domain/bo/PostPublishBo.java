package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "发布动态")
public class PostPublishBo {
    private Long storeId;
    private Long petId;
    private String userName;
    @NotBlank(message = "内容不能为空")
    private String content;
    private String mediaUrls;
    private Integer mediaType;
    private Integer visibility;
    private Integer allowLike;
    private Integer allowComment;
}
