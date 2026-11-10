package com.sqx.app.pet.datacenter.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "数据中心经营概览")
public class DataCenterOverviewVo {

    @Schema(description = "今日营收（商城+投喂实付）")
    private BigDecimal todayRevenue;
    @Schema(description = "昨日营收")
    private BigDecimal yesterdayRevenue;

    @Schema(description = "今日商城成交笔数")
    private Long todayMallOrderCount;
    @Schema(description = "昨日商城成交笔数")
    private Long yesterdayMallOrderCount;
    @Schema(description = "今日商城实付金额")
    private BigDecimal todayMallAmount;
    @Schema(description = "今日客单价（商城）")
    private BigDecimal todayAvgOrderAmount;

    @Schema(description = "今日投喂笔数")
    private Long todayFeedCount;
    @Schema(description = "昨日投喂笔数")
    private Long yesterdayFeedCount;
    @Schema(description = "今日投喂金额")
    private BigDecimal todayFeedAmount;
    @Schema(description = "昨日投喂金额")
    private BigDecimal yesterdayFeedAmount;

    @Schema(description = "今日新增用户")
    private Long todayNewUserCount;
    @Schema(description = "昨日新增用户")
    private Long yesterdayNewUserCount;

    @Schema(description = "待审核动态")
    private Long communityPending;
    @Schema(description = "待审核领养")
    private Long adoptPending;
    @Schema(description = "待反馈投喂")
    private Long feedPending;
    @Schema(description = "待处理投诉")
    private Long complaintPending;
    @Schema(description = "7日内即将过期可用券")
    private Long couponExpiring;

    @Schema(description = "社区动态总数")
    private Long communityTotal;
    @Schema(description = "今日新增动态")
    private Long communityToday;
    @Schema(description = "宠物档案数")
    private Long archiveCount;
    @Schema(description = "上架商品数")
    private Long mallOnCount;

    @Schema(description = "近7日趋势（含今日）")
    private List<DataCenterTrendDayVo> trend7d;
}
