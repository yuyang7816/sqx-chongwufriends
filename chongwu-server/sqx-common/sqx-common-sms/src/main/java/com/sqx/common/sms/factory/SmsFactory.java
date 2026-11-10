package com.sqx.common.sms.factory;

import com.sqx.common.core.exception.BizException;
import com.sqx.common.sms.provider.ISmsProvider;
import com.sqx.common.sms.enums.SmsError;
import com.sqx.common.sms.enums.SmsProviderEnum;
import com.sqx.common.sms.provider.AliyunSmsProvider;
import com.sqx.common.sms.provider.DXBSmsProvider;
import com.sqx.common.sms.provider.TencentSmsProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chengming
 * @since 2025/6/11
 */
@Component
public class SmsFactory {

    private static final Map<String, ISmsProvider> smsServiceCache = new ConcurrentHashMap<>();

    /** 默认短信平台：短信宝 */
    public static final String DEFAULT_PROVIDER = SmsProviderEnum.DXB.getCode();

    /**
     * 获取sms服务
     */
    public static ISmsProvider getSmsService(String providerName) {
        String normalized = normalizeProvider(providerName);
        if (smsServiceCache.containsKey(normalized)) {
            return smsServiceCache.get(normalized);
        }
        ISmsProvider smsService = createSmsService(normalized);
        smsServiceCache.put(normalized, smsService);
        return smsService;
    }

    /**
     * 兼容后台配置：dxb / 3 / 短信宝、aliyun / 2、tencent / 1
     */
    public static String normalizeProvider(String providerName) {
        if (StringUtils.isBlank(providerName)) {
            return DEFAULT_PROVIDER;
        }
        String raw = providerName.trim();
        String lower = raw.toLowerCase();
        if ("3".equals(lower) || "dxb".equals(lower) || "smsbao".equals(lower) || "短信宝".equals(raw)) {
            return SmsProviderEnum.DXB.getCode();
        }
        if ("2".equals(lower) || "aliyun".equals(lower) || "ali".equals(lower) || "阿里云".equals(raw) || "阿里云短信平台".equals(raw)) {
            return SmsProviderEnum.ALIYUN.getCode();
        }
        if ("1".equals(lower) || "tencent".equals(lower) || "qcloud".equals(lower) || "腾讯".equals(raw) || "腾讯云短信平台".equals(raw)) {
            return SmsProviderEnum.TENCENT.getCode();
        }
        return raw;
    }

    private static ISmsProvider createSmsService(String providerName) {
        if (Objects.equals(providerName, SmsProviderEnum.DXB.getCode())) {
            return new DXBSmsProvider();
        } else if (Objects.equals(providerName, SmsProviderEnum.ALIYUN.getCode())) {
            return new AliyunSmsProvider();
        } else if (Objects.equals(providerName, SmsProviderEnum.TENCENT.getCode())) {
            return new TencentSmsProvider();
        } else {
            throw new BizException(SmsError.NOT_SUPPORT_PROVIDER);
        }
    }
}
