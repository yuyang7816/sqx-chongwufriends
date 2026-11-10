package com.sqx.app.pet.mall.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "MallSpuVo")
public class MallSpuVo implements Serializable {
    private Long id;
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
    private Long defaultSkuId;
    private List<Long> specIds;
    private List<MallSkuVo> skuList;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
