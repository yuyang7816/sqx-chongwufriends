package com.sqx.app.pet.strategy.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "PetStrategySaveBo")
public class PetStrategySaveBo {
    private Long categoryId;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String subTitle;
    private String tag;
    private String coverUrl;
    @NotBlank(message = "正文不能为空")
    private String content;
    private Integer sort;
    @NotNull(message = "状态不能为空")
    private Integer status;
}
