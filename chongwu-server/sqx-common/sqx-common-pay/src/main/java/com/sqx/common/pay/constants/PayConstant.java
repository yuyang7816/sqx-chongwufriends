package com.sqx.common.pay.constants;

import java.util.List;
import java.util.Map;

/**
 * @author chengming
 * @since 2026/2/11
 */
public interface PayConstant {

    // 微信
    interface Wx {
        String MCH_ID = "pay_wx_mch_id";
        String MCH_KEY = "pay_wx_mch_key";
        String KEY_PATH = "pay_wx_key_path";
        String JSAPI_APP_ID = "pay_wx_jsapi_app_id";
        String JSAPI_MCH_ID = "pay_wx_jsapi_mch_id";
        String JSAPI_MCH_KEY = "pay_wx_jsapi_api_key";
        String H5_MCH_ID = "pay_wx_h5_mch_id";

        // 用户端
        interface User {
            // app appid
            String APP_ID = "pay_wx_app_id";

            // 公众号appid
            String MP_APP_ID = "pay_wx_h5_app_id";

            // 小程序appid
            String MA_APP_ID = "sys_user_ma_appid";
        }

        // 其他端
    }
}
