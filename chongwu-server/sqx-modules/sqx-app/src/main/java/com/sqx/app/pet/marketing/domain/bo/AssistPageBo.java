package com.sqx.app.pet.marketing.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "我的助力分页")
public class AssistPageBo extends PageParam {
    @Schema(description = "方向：mine 我助力的 / received 助力我的，默认 mine")
    private String direction;
}
