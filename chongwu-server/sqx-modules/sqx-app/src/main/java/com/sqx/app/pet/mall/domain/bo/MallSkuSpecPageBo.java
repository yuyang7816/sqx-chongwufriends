package com.sqx.app.pet.mall.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "规格模板分页")
public class MallSkuSpecPageBo extends PageParam {
    private String keyword;
    private Integer status;
}
