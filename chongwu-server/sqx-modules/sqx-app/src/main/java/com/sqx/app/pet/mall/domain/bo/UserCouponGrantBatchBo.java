package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "批量向用户发放优惠券")
public class UserCouponGrantBatchBo {
    @NotEmpty(message = "至少选择一个用户")
    private List<Long> userIds;
    @NotNull(message = "券模板ID不能为空")
    private Long templateId;
    private Integer count;
}
