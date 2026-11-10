package com.sqx.common.sms.provider;

import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.Md5Utils;
import com.sqx.common.core.utils.http.OkHttpUtil;
import com.sqx.common.redis.utils.ConfigUtil;
import com.sqx.common.sms.domain.ConvertPhone;
import com.sqx.common.sms.enums.SmsError;
import com.sqx.common.sms.enums.SmsProviderEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 短信宝服务
 *
 * @author chengming
 * @since 2025/6/11
 */
@Slf4j
public class DXBSmsProvider implements ISmsProvider {

    @Override
    public String getProviderName() {
        return SmsProviderEnum.DXB.getDesc();
    }

    @Override
    public void sendSms(String phone, String content) {
        String chinaUrl = "https://api.smsbao.com/sms";
        String internationalUrl = "https://api.smsbao.com/wsms";

        String username = ConfigUtil.getConfigValue("sms_smsbao_username");
        String password = ConfigUtil.getConfigValue("sms_smsbao_password");
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BizException("短信宝账号或密码未配置");
        }

        ConvertPhone convertPhone = convertPhoneNo(phone);

        Map<String, String> params = new HashMap<>();
        params.put("u", username);
        params.put("p", Md5Utils.md5s(password));
        params.put("m", convertPhone.getEncodePhone());
        params.put("c", content);

        String resp;
        if (Boolean.TRUE.equals(convertPhone.getChina())) {
            resp = OkHttpUtil.doGet(chinaUrl, params);
        } else {
            resp = OkHttpUtil.doGet(internationalUrl, params);
        }
        log.info("[sms] - 短信宝返回值: {}", resp);

        if ("0".equals(StringUtils.trimToEmpty(resp))) {
            log.info("[sms] - 短信发送成功");
            return;
        }

        throw resolveDxbError(resp);
    }

    private BizException resolveDxbError(String resp) {
        String code = StringUtils.trimToEmpty(resp);
        switch (code) {
            case "30":
                log.error("[sms] - 短信宝密码错误");
                return new BizException(SmsError.PASSWORD_ERROR);
            case "40":
                log.error("[sms] - 短信宝账号不存在");
                return new BizException(SmsError.USERNAME_ERROR);
            case "41":
                log.error("[sms] - 短信宝余额不足");
                return new BizException(SmsError.BALANCE_ERROR);
            case "43":
                log.error("[sms] - 短信宝IP地址限制");
                return new BizException(SmsError.IP_LIMITATION);
            case "50":
                log.error("[sms] - 短信内容含有敏感词");
                return new BizException(SmsError.SENSITIVE_WORDS);
            case "51":
                log.error("[sms] - 短信手机号码不正确");
                return new BizException(SmsError.PHONE_ERROR);
            default:
                log.error("[sms] - 未知异常 code={}", code);
                return new BizException(SmsError.SMS_SEND_ERROR);
        }
    }

    private ConvertPhone convertPhoneNo(String phone) {
        ConvertPhone convertPhone = new ConvertPhone();
        if (phone.contains("+86") || !phone.contains("+")) {
            convertPhone.setEncodePhone(phone.replace("+86", ""));
            convertPhone.setChina(Boolean.TRUE);
        } else {
            try {
                convertPhone.setEncodePhone(URLEncoder.encode(phone, "UTF-8"));
                convertPhone.setChina(Boolean.FALSE);
            } catch (UnsupportedEncodingException e) {
                throw new BizException("发送失败");
            }
        }
        return convertPhone;
    }
}
