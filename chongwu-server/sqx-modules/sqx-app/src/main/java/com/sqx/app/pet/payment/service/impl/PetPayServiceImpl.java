package com.sqx.app.pet.payment.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.sqx.app.base.pay.enums.BusinessType;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.utils.RequestUtil;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.feed.db.service.IFeedOrderService;
import com.sqx.app.pet.feed.domain.entity.FeedOrder;
import com.sqx.app.pet.mall.db.service.IMallOrderService;
import com.sqx.app.pet.mall.domain.entity.MallOrder;
import com.sqx.app.pet.payment.config.PetPayProperties;
import com.sqx.app.pet.payment.db.service.IPayOrderService;
import com.sqx.app.pet.payment.domain.bo.PetPayCreateBo;
import com.sqx.app.pet.payment.domain.bo.PetPayQueryBo;
import com.sqx.app.pet.payment.domain.entity.PayOrder;
import com.sqx.app.pet.payment.domain.vo.PetPayChannelVo;
import com.sqx.app.pet.payment.domain.vo.PetPayOrderVo;
import com.sqx.app.pet.payment.service.PetPayService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.GenUtil;
import com.sqx.common.pay.constants.PayConstant;
import com.sqx.common.pay.domain.UnifiedPayRequest;
import com.sqx.common.pay.enums.PayChannel;
import com.sqx.common.pay.enums.PayTradeType;
import com.sqx.common.pay.notify.IPayNotifyBusinessService;
import com.sqx.common.pay.service.IPayService;
import com.sqx.common.redis.utils.ConfigUtil;
import com.sqx.common.redis.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;

@Slf4j
@Service
public class PetPayServiceImpl implements PetPayService {

    private static final String MOCK_CHANNEL = "mock";
    private static final String MOCK_CONFIG_KEY = "pet_pay_mock_enabled";
    private static final String NOTIFY_URL = "/app/pet/payment/notify/%s";
    private static final String CONTEXT_PATH = "/sqx_fast";
    private static final String WAP_CACHE_KEY = "pet:pay:wap:";

    @Resource
    private IPayOrderService iPayOrderService;
    @Resource
    private IMallOrderService iMallOrderService;
    @Resource
    private IFeedOrderService iFeedOrderService;
    @Resource
    private IPayNotifyBusinessService payNotifyBusinessService;
    @Resource
    private IPayService iPayService;
    @Resource
    private IUserService iUserService;

    @Override
    public PetPayChannelVo getChannels() {
        boolean mockEnabled = isMockEnabled();
        boolean wechatConfigured = mockEnabled || isWechatConfigured();
        boolean alipayConfigured = mockEnabled || isAlipayConfigured();
        String terminal = resolveClientTerminal();

        PetPayChannelVo vo = new PetPayChannelVo();
        vo.setMockEnabled(mockEnabled);
        // 小程序仅微信；H5 仅支付宝；APP 双渠道
        if ("mini".equals(terminal)) {
            // 小程序仅微信；未配置时创建支付会走 mock 兜底
            vo.setWechatEnabled(true);
            vo.setAlipayEnabled(false);
        } else if ("h5".equals(terminal)) {
            vo.setWechatEnabled(false);
            vo.setAlipayEnabled(alipayConfigured);
        } else {
            vo.setWechatEnabled(wechatConfigured);
            vo.setAlipayEnabled(alipayConfigured);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PetPayOrderVo createPayOrder(Long userId, PetPayCreateBo bo) {
        BusinessType.fromCode(bo.getBusinessType());
        BigDecimal amount;
        if (Objects.equals(bo.getBusinessType(), BusinessType.MALL_ORDER.getCode())) {
            MallOrder order = iMallOrderService.lambdaQuery().eq(MallOrder::getOrderNo, bo.getBizOrderNo()).one();
            if (order == null || !Objects.equals(order.getUserId(), userId)) {
                throw new BizException(PetError.ORDER_NOT_FOUND);
            }
            if (Objects.equals(order.getPayStatus(), 3)) {
                throw new BizException("订单已支付");
            }
            amount = order.getPayAmount();
        } else if (Objects.equals(bo.getBusinessType(), BusinessType.FEED_ORDER.getCode())) {
            FeedOrder order = iFeedOrderService.lambdaQuery().eq(FeedOrder::getOrderNo, bo.getBizOrderNo()).one();
            if (order == null || !Objects.equals(order.getUserId(), userId)) {
                throw new BizException(PetError.ORDER_NOT_FOUND);
            }
            amount = order.getAmount();
        } else {
            throw new BizException("不支持的业务类型");
        }

        PayOrder payOrder = new PayOrder();
        payOrder.setPayNo(GenUtil.genOrderNo());
        payOrder.setBizOrderNo(bo.getBizOrderNo());
        payOrder.setUserId(userId);
        payOrder.setBusinessType(bo.getBusinessType());
        payOrder.setTerminal(bo.getTerminal() != null ? bo.getTerminal() : "mini");
        validateChannelForTerminal(bo.getPayChannel(), payOrder.getTerminal());
        String channel = useMockChannel(bo.getPayChannel()) ? MOCK_CHANNEL : normalizePayChannel(bo.getPayChannel());
        payOrder.setPayChannel(channel);
        payOrder.setAmount(amount == null ? BigDecimal.ZERO : amount);
        payOrder.setStatus(1);
        iPayOrderService.save(payOrder);

        PetPayOrderVo vo = new PetPayOrderVo();
        BeanUtils.copyProperties(payOrder, vo);
        if (MOCK_CHANNEL.equals(channel)) {
            vo.setPayParams("{\"mock\":true,\"payNo\":\"" + payOrder.getPayNo() + "\"}");
            return vo;
        }

        try {
            String terminal = payOrder.getTerminal();
            String tradeType = resolveTradeType(channel, terminal);
            UnifiedPayRequest request = buildUnifiedPayRequest(payOrder, bo, userId, channel, tradeType);
            String rawParams = iPayService.unifiedPay(request);
            String payParams = formatPayParams(channel, tradeType, rawParams, payOrder.getPayNo());
            vo.setPayParams(payParams);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("[宠物支付] 创建支付失败 payNo={}", payOrder.getPayNo(), e);
            throw new BizException("支付创建失败：" + StringUtils.defaultIfBlank(e.getMessage(), "请检查系统支付配置"));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PetPayOrderVo queryStatus(Long userId, PetPayQueryBo bo) {
        PayOrder payOrder = iPayOrderService.lambdaQuery().eq(PayOrder::getPayNo, bo.getPayNo()).one();
        if (payOrder == null || !Objects.equals(payOrder.getUserId(), userId)) {
            throw new BizException(PetError.PAY_ORDER_NOT_FOUND);
        }
        if (MOCK_CHANNEL.equals(payOrder.getPayChannel()) && Objects.equals(payOrder.getStatus(), 1)) {
            payNotifyBusinessService.onPaymentSuccess(MOCK_CHANNEL, payOrder.getPayNo(), "MOCK_" + payOrder.getPayNo());
            payOrder = iPayOrderService.lambdaQuery().eq(PayOrder::getPayNo, bo.getPayNo()).one();
        }
        PetPayOrderVo vo = new PetPayOrderVo();
        BeanUtils.copyProperties(payOrder, vo);
        return vo;
    }

    @Override
    public String getAlipayWapHtml(String payNo) {
        if (StringUtils.isBlank(payNo)) {
            throw new BizException("支付单号不能为空");
        }
        PayOrder payOrder = iPayOrderService.lambdaQuery().eq(PayOrder::getPayNo, payNo).one();
        if (payOrder == null) {
            throw new BizException(PetError.PAY_ORDER_NOT_FOUND);
        }
        if (!Objects.equals(payOrder.getPayChannel(), PayChannel.ALI.getValue())) {
            throw new BizException("非支付宝支付单");
        }
        if (!Objects.equals(payOrder.getStatus(), 1)) {
            throw new BizException("支付单状态不可支付");
        }
        String html = RedisUtil.getCacheObject(WAP_CACHE_KEY + payNo);
        if (StringUtils.isBlank(html)) {
            throw new BizException("支付页面已过期，请重新发起支付");
        }
        return html;
    }

    private UnifiedPayRequest buildUnifiedPayRequest(PayOrder payOrder, PetPayCreateBo bo, Long userId,
                                                     String channel, String tradeType) {
        String domain = resolvePayDomain();
        String notifyUrl = buildApiUrl(domain, String.format(NOTIFY_URL, channel));
        String returnUrl = buildApiUrl(domain, "/packageA/payment/result?success=1&payNo=" + payOrder.getPayNo()
                + "&orderNo=" + payOrder.getBizOrderNo());

        UnifiedPayRequest.UnifiedPayRequestBuilder builder = UnifiedPayRequest.builder()
                .orderNo(payOrder.getPayNo())
                .amount(payOrder.getAmount())
                .channel(channel)
                .tradeType(tradeType)
                .subject(resolvePaySubject(bo.getBusinessType()))
                .clientIp("127.0.0.1")
                .notifyUrl(notifyUrl)
                .returnUrl(returnUrl);

        if (Objects.equals(channel, PayChannel.WX.getValue())) {
            String appId = ConfigUtil.getConfigValueOrDefault(PetPayProperties.WX_MINI_APP_ID);
            if (StringUtils.isBlank(appId)) {
                appId = ConfigUtil.getConfigValueOrDefault("sys_user_ma_appid");
            }
            if (StringUtils.isBlank(appId)) {
                throw new BizException("微信小程序 AppId 未配置");
            }
            builder.appId(appId);
            UserEntity user = iUserService.getById(userId);
            if (user == null || StringUtils.isBlank(user.getMaOpenId())) {
                throw new BizException("请先使用微信授权登录后再支付");
            }
            builder.openId(user.getMaOpenId());
        }
        return builder.build();
    }

    private String resolvePayDomain() {
        String domain = ConfigUtil.getConfigValueOrDefault("sys_h5_domain");
        if (StringUtils.isBlank(domain)) {
            domain = ConfigUtil.getConfigValueOrDefault("sys_admin_domain");
        }
        if (StringUtils.isBlank(domain)) {
            throw new BizException("系统域名未配置（sys_h5_domain），无法发起支付");
        }
        return domain.trim().replaceAll("/+$", "");
    }

    private String buildApiUrl(String domain, String path) {
        String normalizedDomain = domain.replaceAll("/+$", "");
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        if (!normalizedDomain.endsWith(CONTEXT_PATH)) {
            normalizedDomain = normalizedDomain + CONTEXT_PATH;
        }
        return normalizedDomain + normalizedPath;
    }

    private String formatPayParams(String channel, String tradeType, String rawParams, String payNo) {
        JSONObject payload = new JSONObject();
        if (Objects.equals(channel, PayChannel.ALI.getValue())) {
            if (Objects.equals(tradeType, PayTradeType.H5.getCode())) {
                payload.put("payType", "wap");
                payload.put("payForm", rawParams);
                RedisUtil.setCacheObject(WAP_CACHE_KEY + payNo, rawParams, Duration.ofMinutes(30));
            } else {
                payload.put("payType", "app");
                payload.put("orderStr", rawParams);
            }
            return payload.toJSONString();
        }

        if (Objects.equals(channel, PayChannel.WX.getValue())) {
            payload.put("payType", "jsapi");
            try {
                JSONObject wx = JSONObject.parseObject(rawParams);
                if (wx.containsKey("packageValue") && !wx.containsKey("package")) {
                    wx.put("package", wx.getString("packageValue"));
                }
                payload.putAll(wx);
            } catch (Exception e) {
                payload.put("raw", rawParams);
            }
            return payload.toJSONString();
        }

        payload.put("raw", rawParams);
        return payload.toJSONString();
    }

    private String resolvePaySubject(String businessType) {
        if (Objects.equals(businessType, BusinessType.MALL_ORDER.getCode())) {
            return "商城订单支付";
        }
        if (Objects.equals(businessType, BusinessType.FEED_ORDER.getCode())) {
            return "投喂订单支付";
        }
        return "订单支付";
    }

    private String resolveTradeType(String channel, String terminal) {
        if (Objects.equals(channel, PayChannel.ALI.getValue())) {
            if ("app".equalsIgnoreCase(terminal)) {
                return PayTradeType.APP.getCode();
            }
            if ("h5".equalsIgnoreCase(terminal)) {
                return PayTradeType.H5.getCode();
            }
            return PayTradeType.H5.getCode();
        }
        // 微信侧无 H5 交易类型（合法值为 JSAPI/NATIVE/APP/MWEB），小程序/公众号统一走 JSAPI
        return PayTradeType.JSAPI.getCode();
    }

    private String normalizePayChannel(String payChannel) {
        if (StringUtils.isBlank(payChannel)) {
            throw new BizException("支付渠道不能为空");
        }
        String value = payChannel.trim().toLowerCase();
        if ("alipay".equals(value) || PayChannel.ALI.getValue().equals(value)) {
            if (!isAlipayConfigured() && !isMockEnabled()) {
                throw new BizException("支付宝支付未配置");
            }
            return PayChannel.ALI.getValue();
        }
        if ("wechat".equals(value) || "wx".equals(value) || PayChannel.WX.getValue().equals(value)) {
            if (!isWechatConfigured() && !isMockEnabled()) {
                throw new BizException("微信支付未配置");
            }
            return PayChannel.WX.getValue();
        }
        throw new BizException("不支持的支付渠道");
    }

    private boolean useMockChannel(String payChannel) {
        if (MOCK_CHANNEL.equalsIgnoreCase(payChannel)) {
            return true;
        }
        if (isMockEnabled()) {
            return true;
        }
        if (isWechatChannel(payChannel) && !isWechatConfigured()) {
            return true;
        }
        if (isAlipayChannel(payChannel) && !isAlipayConfigured()) {
            return true;
        }
        return false;
    }

    private boolean isWechatChannel(String payChannel) {
        String value = StringUtils.defaultString(payChannel).toLowerCase();
        return "wechat".equals(value) || "wx".equals(value);
    }

    private boolean isAlipayChannel(String payChannel) {
        String value = StringUtils.defaultString(payChannel).toLowerCase();
        return "alipay".equals(value) || "ali".equals(value);
    }

    private void validateChannelForTerminal(String payChannel, String terminal) {
        if (StringUtils.isBlank(payChannel) || MOCK_CHANNEL.equalsIgnoreCase(payChannel)) {
            return;
        }
        String value = payChannel.trim().toLowerCase();
        String t = StringUtils.defaultIfBlank(terminal, "mini").trim().toLowerCase();
        boolean alipay = "alipay".equals(value) || PayChannel.ALI.getValue().equals(value) || "ali".equals(value);
        boolean wechat = "wechat".equals(value) || "wx".equals(value) || PayChannel.WX.getValue().equals(value);
        if ("mini".equals(t) && alipay) {
            throw new BizException("小程序仅支持微信支付");
        }
        if ("h5".equals(t) && wechat) {
            throw new BizException("H5仅支持支付宝支付");
        }
    }

    private String resolveClientTerminal() {
        String clientType = StringUtils.defaultString(RequestUtil.getClientType()).toUpperCase();
        if (clientType.contains("MP-WEIXIN") || clientType.contains("MINI")) {
            return "mini";
        }
        if (clientType.contains("H5") || clientType.contains("WEB")) {
            return "h5";
        }
        if (clientType.contains("APP")) {
            return "app";
        }
        return "mini";
    }

    private boolean isMockEnabled() {
        return "1".equals(ConfigUtil.getConfigValueOrDefault(MOCK_CONFIG_KEY));
    }

    private boolean isWechatConfigured() {
        return StringUtils.isNotBlank(ConfigUtil.getConfigValueOrDefault(PetPayProperties.WX_MINI_MCH_ID))
                || StringUtils.isNotBlank(ConfigUtil.getConfigValueOrDefault(PayConstant.Wx.MCH_ID));
    }

    private boolean isAlipayConfigured() {
        return StringUtils.isNotBlank(ConfigUtil.getConfigValueOrDefault("pay_ali_appid"))
                && StringUtils.isNotBlank(ConfigUtil.getConfigValueOrDefault("pay_ali_private_key"));
    }
}
