package com.sqx.app.pet.mall.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "MallSpuSaveBo")
public class MallSpuSaveBo {
    @NotBlank(message = "商品名称不能为空")
    private String name;
    private String category;
    private Long categoryId;
    private String coverUrl;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal activityPrice;
    private BigDecimal vipPrice;
    private Integer pointPrice;
    private Integer bizType;
    private Integer status;
    private Integer sort;
    @Schema(description = "1单规格 2多规格")
    private Integer specMode;
    @Schema(description = "单规格SKU信息")
    private MallSpuSingleSkuBo singleSku;
    @Schema(description = "多规格模板ID列表")
    private List<Long> specIds;
    @Schema(description = "多规格SKU列表（笛卡尔积后的售价/积分/库存）")
    private List<MallSpuSkuBo> skuList;
}
