package com.sqx.common.pay.channel.wechat.manager;

import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.sqx.common.pay.channel.wechat.domain.WxPayConfig;
import com.sqx.common.pay.channel.wechat.service.WxPayConfigService;
import com.sqx.common.pay.constants.PayConstant;
import com.sqx.common.redis.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chengming
 * @since 2026/1/9
 */
@Slf4j
@Component
public class WxPayServiceManager {

    @Resource
    private WxPayConfigService wxPayConfigService;

    private final Map<String, WxPayService> wxPayServiceMap = new ConcurrentHashMap<>();

    public void initializeWxPayServices() {
        wxPayServiceMap.clear();
        List<WxPayConfig> wxPayConfigs = wxPayConfigService.getWxPayConfigs();

        for (WxPayConfig wxPayConfig : wxPayConfigs) {
            registerServiceIfPresent(wxPayConfig.getAppId(), "APP",
                    PayConstant.Wx.MCH_ID, PayConstant.Wx.MCH_KEY);
            registerServiceIfPresent(wxPayConfig.getH5AppId(), "H5",
                    PayConstant.Wx.H5_MCH_ID, PayConstant.Wx.MCH_KEY);
            registerServiceIfPresent(wxPayConfig.getJsAppId(), "公众号/JSAPI",
                    PayConstant.Wx.JSAPI_MCH_ID, PayConstant.Wx.JSAPI_MCH_KEY);
        }

        String jsapiAppId = ConfigUtil.getConfigValueOrDefault(PayConstant.Wx.JSAPI_APP_ID);
        if (StringUtils.isNotBlank(jsapiAppId) && !wxPayServiceMap.containsKey(jsapiAppId)) {
            registerServiceIfPresent(jsapiAppId, "小程序JSAPI",
                    PayConstant.Wx.JSAPI_MCH_ID, PayConstant.Wx.JSAPI_MCH_KEY);
        }

        log.info("已初始化 {} 个微信支付客户端", wxPayServiceMap.size());
    }

    private void registerServiceIfPresent(String appId, String desc, String mchIdKey, String mchKeyKey) {
        if (StringUtils.isNotBlank(appId)) {
            WxPayService service = createWxPayService(appId, mchIdKey, mchKeyKey);
            wxPayServiceMap.put(appId, service);
            log.info("注册微信支付客户端: {} (appId={}, mchIdKey={})", desc, appId, mchIdKey);
        }
    }

    private WxPayService createWxPayService(String appId, String mchIdKey, String mchKeyKey) {
        com.github.binarywang.wxpay.config.WxPayConfig config = new com.github.binarywang.wxpay.config.WxPayConfig();

        String mchId = resolveConfigValue(mchIdKey, PayConstant.Wx.MCH_ID);
        String mchKey = resolveConfigValue(mchKeyKey, PayConstant.Wx.MCH_KEY);
        String keyPath = ConfigUtil.getConfigValueOrDefault(PayConstant.Wx.KEY_PATH);

        config.setAppId(appId);
        config.setMchId(mchId);
        config.setMchKey(mchKey);
        config.setKeyPath(keyPath);

        WxPayService service = new WxPayServiceImpl();
        service.setConfig(config);
        return service;
    }

    private String resolveConfigValue(String primaryKey, String fallbackKey) {
        String value = ConfigUtil.getConfigValueOrDefault(primaryKey);
        if (StringUtils.isBlank(value)) {
            value = ConfigUtil.getConfigValueOrDefault(fallbackKey);
        }
        return StringUtils.trimToEmpty(value);
    }

    /**
     * 根据 appId 获取对应的微信支付客户端
     *
     * @param appId 微信 AppID（小程序/公众号/H5）
     * @return WxPayService 实例
     * @throws IllegalArgumentException 如果 appId 未配置或未初始化
     */
    public WxPayService getWxPayService(String appId) {
        WxPayService service = wxPayServiceMap.get(appId);
        if (service == null) {
            throw new IllegalArgumentException("未找到 appId=" + appId + " 对应的微信支付客户端，请检查配置");
        }

        return service;
    }
}
