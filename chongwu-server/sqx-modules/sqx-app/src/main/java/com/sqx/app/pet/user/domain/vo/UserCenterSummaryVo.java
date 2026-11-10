package com.sqx.app.pet.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "个人中心汇总")
public class UserCenterSummaryVo implements Serializable {
    @Schema(description = "可用优惠券数量")
    private Integer couponAvailable;
    @Schema(description = "待反馈赠送数量")
    private Integer giftPendingFeedback;
    @Schema(description = "累计助力次数")
    private Integer assistCount;
    @Schema(description = "进行中兑换数量")
    private Integer exchangePending;
}
