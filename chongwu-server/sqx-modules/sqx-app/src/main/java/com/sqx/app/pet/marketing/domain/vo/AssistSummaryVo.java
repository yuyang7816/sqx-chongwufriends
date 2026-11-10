package com.sqx.app.pet.marketing.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "助力汇总")
public class AssistSummaryVo implements Serializable {
    @Schema(description = "累计助力次数（我助力的）")
    private Integer totalCount;
    @Schema(description = "收获助力次数（助力我的）")
    private Integer receivedCount;
}
