package com.sqx.app.pet.community.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "动态分页")
public class PostPageBo extends PageParam {
    private Long storeId;
    private Long petId;
    private Long userId;
    @Schema(description = "审核状态 0待审 1通过 2拒绝")
    private Integer auditStatus;
}
