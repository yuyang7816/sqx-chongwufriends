package com.sqx.app.pet.marketing.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "榜单分页")
public class RankingPageBo extends PageParam {
    private Long periodId;
    /** 0=本周 -1=上周，与 periodId 二选一，periodId 优先 */
    private Integer weekOffset;
}
