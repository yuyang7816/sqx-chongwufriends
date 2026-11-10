package com.sqx.app.pet.mall.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "MallOrderVo")
public class MallOrderVo implements Serializable {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long spuId;
    private Long skuId;
    private Integer quantity;
    private BigDecimal payAmount;
    private Integer pointAmount;
    private Integer payType;
    private Integer bizType;
    private Integer payStatus;
    private Integer status;
    private String remark;
    private String spuName;
    private String coverUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
