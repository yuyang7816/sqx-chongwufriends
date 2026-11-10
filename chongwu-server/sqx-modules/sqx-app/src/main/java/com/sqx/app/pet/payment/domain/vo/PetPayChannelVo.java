package com.sqx.app.pet.payment.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "可用支付渠道")
public class PetPayChannelVo implements Serializable {
    @Schema(description = "是否支持微信支付")
    private Boolean wechatEnabled;
    @Schema(description = "是否支持支付宝支付")
    private Boolean alipayEnabled;
    @Schema(description = "是否沙箱/mock支付")
    private Boolean mockEnabled;
}
