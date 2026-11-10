package com.sqx.app.pet.mall.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "MallSpuPageBo")
public class MallSpuPageBo extends PageParam {
    private String category;
    private Long categoryId;
    private Integer bizType;
    private Integer status;
    private String keyword;
}
