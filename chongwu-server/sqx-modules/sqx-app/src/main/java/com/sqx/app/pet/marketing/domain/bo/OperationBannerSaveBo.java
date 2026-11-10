package com.sqx.app.pet.marketing.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "OperationBannerSaveBo")
public class OperationBannerSaveBo {
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "副标题")
    private String subTitle;

    @NotBlank(message = "图片不能为空")
    private String imageUrl;

    @Schema(description = "跳转链接")
    private String jumpUrl;

    @Schema(description = "位置编码 home/shop/adopt 等")
    private String positionCode;

    @Schema(description = "排序，越小越靠前")
    private Integer sort;

    @Schema(description = "状态 1启用 2禁用")
    private Integer status;
}
