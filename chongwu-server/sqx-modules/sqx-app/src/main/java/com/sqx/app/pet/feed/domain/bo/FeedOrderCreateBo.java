package com.sqx.app.pet.feed.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "创建投喂订单")
public class FeedOrderCreateBo {
    @NotNull(message = "宠物ID不能为空")
    private Long petId;
    /** 兼容旧客户端；有 items 时由服务端按商城价汇总覆盖 */
    private String productName;
    private BigDecimal amount;
    private Integer quantity;
    private String message;
    private String signature;
    @Valid
    private List<FeedOrderItemBo> items;
}
