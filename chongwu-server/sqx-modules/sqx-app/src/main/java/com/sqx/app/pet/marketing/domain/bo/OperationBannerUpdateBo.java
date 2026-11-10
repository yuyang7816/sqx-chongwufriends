package com.sqx.app.pet.marketing.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "OperationBannerUpdateBo")
public class OperationBannerUpdateBo {
    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "副标题")
    private String subTitle;

    @NotBlank(message = "图片不能为空")
    private String imageUrl;

    @Schema(description = "跳转链接")
    private String jumpUrl;

    @Schema(description = "位置编码")
    private String positionCode;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态 1启用 2禁用")
    private Integer status;
}
