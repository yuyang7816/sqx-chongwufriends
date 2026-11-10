package com.sqx.app.pet.feed.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "上传投喂反馈视频")
public class FeedFeedbackUploadBo {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    @NotBlank(message = "视频地址不能为空")
    private String videoUrl;
}
