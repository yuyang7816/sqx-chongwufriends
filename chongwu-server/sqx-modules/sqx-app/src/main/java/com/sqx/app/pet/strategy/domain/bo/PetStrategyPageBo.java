package com.sqx.app.pet.strategy.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "PetStrategyPageBo")
public class PetStrategyPageBo extends PageParam {
    @Schema(description = "分类ID")
    private Long categoryId;
    @Schema(description = "标题关键词")
    private String title;
    @Schema(description = "标签关键词")
    private String tag;
    @Schema(description = "状态：1发布 0草稿；为空则不限（管理端）")
    private Integer status;
}
