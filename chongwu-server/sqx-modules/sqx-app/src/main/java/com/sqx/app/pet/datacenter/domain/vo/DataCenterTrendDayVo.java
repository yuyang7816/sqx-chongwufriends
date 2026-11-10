package com.sqx.app.pet.datacenter.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "数据中心近 N 日趋势点")
public class DataCenterTrendDayVo {
    @Schema(description = "日期 yyyy-MM-dd")
    private String date;
    @Schema(description = "当日营收（商城+投喂实付）")
    private BigDecimal revenue;
    @Schema(description = "商城成交笔数")
    private Long mallOrderCount;
    @Schema(description = "投喂金额")
    private BigDecimal feedAmount;
    @Schema(description = "新增用户")
    private Long newUserCount;
}
