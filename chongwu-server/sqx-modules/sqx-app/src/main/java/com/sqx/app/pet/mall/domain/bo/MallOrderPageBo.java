package com.sqx.app.pet.mall.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单分页")
public class MallOrderPageBo extends PageParam {
    @Schema(description = "内部使用，详情Tab可用；列表筛选请用 phone/username")
    private Long userId;
    @Schema(description = "手机号（精确）")
    private String phone;
    @Schema(description = "用户名/昵称（模糊）")
    private String username;
    @Schema(description = "订单号（模糊）")
    private String orderNo;
    private Integer status;
    @Schema(description = "支付状态")
    private Integer payStatus;
    private Integer bizType;
    @Schema(description = "兑换筛选：all/pending/done")
    private String exchangeStatus;
    @Schema(description = "订单类型筛选：all/exchange/product（exchange=bizType=4 或 payType=2；product=除此之外）")
    private String orderType;
}
