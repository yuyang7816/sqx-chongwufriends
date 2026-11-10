package com.sqx.app.base.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Schema(description = "管理端用户360概况")
public class AdminUserOverviewVo implements Serializable {
    private Long userId;
    private String userName;
    private String phone;
    private String avatar;
    private Integer status;
    private Integer userType;
    private Integer isVip;
    private String invitationCode;
    private String superiorInvitationCode;
    private String platform;
    private String createTime;

    private Integer points;
    private Integer coins;
    private Integer flowers;
    private Integer loveValue;
    private Integer level;
    private String levelName;

    private Integer mallOrderCount;
    private BigDecimal mallOrderAmount;
    private Integer feedOrderCount;
    private BigDecimal feedOrderAmount;
    private Integer pendingPayCount;

    private Integer couponAvailable;
    private Integer islandPetCount;
    private Integer giftPendingFeedback;
    private Integer assistCount;
    private Integer exchangePending;
    private Integer complaintCount;
}
