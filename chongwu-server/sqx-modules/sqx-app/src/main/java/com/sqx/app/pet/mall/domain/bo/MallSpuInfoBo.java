package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "MallSpuInfoBo")
public class MallSpuInfoBo {
    @NotNull(message = "商品ID不能为空")
    private Long id;
}
