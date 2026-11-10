package com.sqx.app.pet.mall.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "MallSkuPageBo")
public class MallSkuPageBo extends PageParam {
    @NotNull(message = "商品ID不能为空")
    private Long spuId;
}
