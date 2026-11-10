package com.sqx.app.pet.marketing.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "OperationBannerPageBo")
public class OperationBannerPageBo extends PageParam {
    @Schema(description = "位置编码")
    private String positionCode;

    @Schema(description = "名称关键词")
    private String name;

    @Schema(description = "状态 1启用 2禁用")
    private Integer status;
}
