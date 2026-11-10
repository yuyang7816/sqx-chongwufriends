package com.sqx.app.pet.payment.config;

import org.springframework.stereotype.Component;

/**
 * 宠物业务支付配置键（值从 sys_config 读取）
 */
@Component
public class PetPayProperties {

    public static final String WX_MINI_APP_ID = "pay_wx_jsapi_app_id";
    public static final String WX_MINI_MCH_ID = "pay_wx_jsapi_mch_id";
    public static final String WX_MINI_API_KEY = "pay_wx_jsapi_api_key";
    public static final String WX_H5_APP_ID = "pay_wx_h5_app_id";
    public static final String WX_H5_MCH_ID = "pay_wx_h5_mch_id";
    public static final String ALI_H5_APP_ID = "pay_ali_h5_app_id";
    public static final String ALI_APP_APP_ID = "pay_ali_app_app_id";
}
