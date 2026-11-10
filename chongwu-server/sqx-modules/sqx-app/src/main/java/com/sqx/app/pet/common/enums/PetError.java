package com.sqx.app.pet.common.enums;

import com.sqx.common.core.enums.IErrorCode;

/**
 * 宠物业务错误码 36xxxx
 */
public enum PetError implements IErrorCode {
    INFO_NOT_FOUND(360001, "信息不存在"),
    STORE_NOT_FOUND(360002, "门店不存在"),
    PET_NOT_FOUND(360003, "宠物不存在"),
    CITY_NOT_FOUND(360004, "城市不存在"),
    DUPLICATE_REQUEST(360005, "重复请求，请勿重复提交"),
    ASSET_NOT_FOUND(360006, "用户资产不存在"),
    ISLAND_PET_NOT_FOUND(360007, "岛宅宠物不存在"),
    ADOPT_DUPLICATE(360008, "已提交领养申请"),
    SENSITIVE_WORD(360009, "内容包含敏感词"),
    ASSET_NOT_ENOUGH(360010, "资产余额不足"),
    ASSET_TYPE_INVALID(360011, "资产类型无效"),
    LIKE_NOT_ALLOWED(360012, "该动态不允许点赞"),
    COMMENT_NOT_ALLOWED(360013, "该动态不允许评论"),
    POST_NOT_FOUND(360014, "动态不存在"),
    ASSIST_LIMIT(360015, "今日免费打榜次数已用完，请使用积分或金币继续打榜"),
    ASSIST_PAY_TYPE_REQUIRED(360024, "免费次数已用完，请选择积分或金币打榜"),
    ASSIST_PAY_TYPE_INVALID(360025, "付费打榜方式无效"),
    ASSIST_PAY_COST_INVALID(360026, "付费打榜价格未配置或无效"),

    STOCK_NOT_ENOUGH(360016, "库存不足"),
    ORDER_NOT_FOUND(360017, "订单不存在"),
    PAY_ORDER_NOT_FOUND(360018, "支付单不存在"),
    FEED_COOLDOWN(360019, "投喂冷却中"),
    FEED_BOWL_FILLED(360027, "该盆本轮已投喂，请投喂另一只或等待冷却"),
    TASK_ALREADY_DONE(360020, "任务已完成"),
    COMPLAINT_DUPLICATE(360021, "您已提交过投诉，请等待处理"),
    COMPLAINT_REASON_REQUIRED(360022, "请填写投诉原因"),
    COMPLAINT_TYPE_INVALID(360023, "投诉类型无效"),
    ;

    private final Integer code;
    private final String message;

    PetError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
