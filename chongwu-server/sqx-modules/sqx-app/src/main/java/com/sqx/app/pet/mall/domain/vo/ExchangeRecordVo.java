package com.sqx.app.pet.mall.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "ExchangeRecordVo")
public class ExchangeRecordVo implements Serializable {
    private Long id;
    private String date;
    private String title;
    private Integer points;
    private String status;
    private String orderNo;
    private Integer quantity;
}
