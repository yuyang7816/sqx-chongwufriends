package com.sqx.app.base.verifyCode.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sqx.app.base.template.service.SmsTemplateService;
import com.sqx.app.base.verifyCode.constans.VerifyCodeConstant;
import com.sqx.app.base.verifyCode.db.service.IVerifyCodeService;
import com.sqx.app.base.verifyCode.domain.entity.VerifyCode;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mail.service.IMailService;
import com.sqx.common.redis.utils.ConfigUtil;
import com.sqx.common.redis.utils.RedisUtil;
import com.sqx.common.sms.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 验证码 服务实现类
 *
 * @author sqx
 * @since 2025-04-22 15:31:50
 */
@Slf4j
@Service
public class VerifyCodeServiceImpl implements VerifyCodeService {

    @Autowired
    private IVerifyCodeService iVerifyCodeService;
    @Autowired
    private IMailService mailService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private SmsTemplateService smsTemplateService;

    @Override
    public Boolean verify(String phone, String code) {
        String key = VerifyCodeConstant.CACHE_KEY + phone;
        if (!RedisUtil.hasKey(key)) {
            return false;
        }

        String cacheCode = RedisUtil.getCacheObject(key);
        if (!Objects.equals(cacheCode, code)) {
            return false;
        }

        RedisUtil.deleteObject(key);

        iVerifyCodeService.update(new VerifyCode(), new LambdaUpdateWrapper<VerifyCode>()
                .eq(VerifyCode::getPhone, phone)
                .eq(VerifyCode::getCode, cacheCode)
                .set(VerifyCode::getStatus, 2)
        );

        return true;
    }

    @Override
    public VerifyCode sendPhoneCode(String phone, String bizCode) {
        String code = createRandomCode();
        String content = getContent(code, bizCode);
        try {
            // 真实发送：失败直接抛错，不写入缓存
            smsService.sendSms(phone, content);
        } catch (BizException ex) {
            log.warn("短信发送失败: phone={}, bizCode={}, err={}", phone, bizCode, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("短信发送异常: phone={}, bizCode={}", phone, bizCode, ex);
            throw new BizException("短信发送失败，请稍后重试");
        }

        if (RedisUtil.hasKey(VerifyCodeConstant.CACHE_KEY + phone)) {
            RedisUtil.deleteObject(VerifyCodeConstant.CACHE_KEY + phone);
        }
        RedisUtil.setCacheObject(VerifyCodeConstant.CACHE_KEY + phone, code, Duration.ofMinutes(10));

        VerifyCode verifyCode = new VerifyCode();
        verifyCode.setPhone(phone);
        verifyCode.setCode(code);
        verifyCode.setStatus(1);
        verifyCode.setExpireTime(LocalDateTime.now().plusMinutes(10));
        iVerifyCodeService.save(verifyCode);
        // 不把验证码回传给前端
        verifyCode.setCode(null);
        return verifyCode;
    }

    @Override
    public VerifyCode sendMailCode(String email, String bizCode) {
        String subject = ConfigUtil.getConfigValue("sys_server_name");

        String code = createRandomCode();
        String content = getContent(code, bizCode);
        try {
            mailService.sendSimpleMail(email, subject, content);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("邮件发送异常: email={}, bizCode={}", email, bizCode, ex);
            throw new BizException("邮件发送失败，请稍后重试");
        }

        if (RedisUtil.hasKey(VerifyCodeConstant.CACHE_KEY + email)) {
            RedisUtil.deleteObject(VerifyCodeConstant.CACHE_KEY + email);
        }

        RedisUtil.setCacheObject(VerifyCodeConstant.CACHE_KEY + email, code, Duration.ofMinutes(10));

        VerifyCode verifyCode = new VerifyCode();
        verifyCode.setPhone(email);
        verifyCode.setCode(code);
        verifyCode.setStatus(1);
        verifyCode.setExpireTime(LocalDateTime.now().plusMinutes(10));
        iVerifyCodeService.save(verifyCode);
        verifyCode.setCode(null);
        return verifyCode;
    }

    private String getContent(String code, String bizCode) {
        String serverName = ConfigUtil.getConfigValue("sys_server_name");
        String contentTemplate = smsTemplateService.getContentByBizCode(bizCode);
        return String.format(contentTemplate, serverName, code);
    }

    private String createRandomCode() {
        int anInt = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(anInt);
    }
}
