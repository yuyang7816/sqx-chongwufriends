package com.sqx.app.pet.payment.service.impl;

import com.sqx.app.base.pay.enums.BusinessType;
import com.sqx.app.pet.feed.db.service.IFeedOrderService;
import com.sqx.app.pet.feed.domain.entity.FeedOrder;
import com.sqx.app.pet.island.service.IslandCareService;
import com.sqx.app.pet.mall.db.service.IMallOrderService;
import com.sqx.app.pet.mall.domain.entity.MallOrder;
import com.sqx.app.pet.payment.db.service.IPayOrderService;
import com.sqx.app.pet.payment.domain.entity.PayOrder;
import com.sqx.app.pet.payment.service.PayCallbackLogService;
import com.sqx.common.pay.notify.IPayNotifyBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Primary
@Service
public class PetPayNotifyBusinessServiceImpl implements IPayNotifyBusinessService {

    @Resource
    private IPayOrderService iPayOrderService;
    @Resource
    private IMallOrderService iMallOrderService;
    @Resource
    private IFeedOrderService iFeedOrderService;
    @Resource
    private PayCallbackLogService payCallbackLogService;
    @Resource
    private IslandCareService islandCareService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onPaymentSuccess(String channel, String outTradeNo, String tradeNo) {
        payCallbackLogService.logCallback(outTradeNo, channel, "success", tradeNo, "ok", 1);
        PayOrder payOrder = iPayOrderService.lambdaQuery().eq(PayOrder::getPayNo, outTradeNo).one();
        if (payOrder == null) {
            log.warn("[支付回调] 未找到支付单 payNo={}", outTradeNo);
            return;
        }
        if (Objects.equals(payOrder.getStatus(), 3)) {
            return;
        }
        payOrder.setStatus(3);
        payOrder.setPayTime(LocalDateTime.now());
        iPayOrderService.updateById(payOrder);
        if (Objects.equals(payOrder.getBusinessType(), BusinessType.MALL_ORDER.getCode())) {
            MallOrder mallOrder = iMallOrderService.lambdaQuery()
                    .eq(MallOrder::getOrderNo, payOrder.getBizOrderNo()).one();
            if (mallOrder != null) {
                mallOrder.setStatus(2);
                mallOrder.setPayStatus(3);
                iMallOrderService.updateById(mallOrder);
            }
        } else if (Objects.equals(payOrder.getBusinessType(), BusinessType.FEED_ORDER.getCode())) {
            FeedOrder feedOrder = iFeedOrderService.lambdaQuery()
                    .eq(FeedOrder::getOrderNo, payOrder.getBizOrderNo()).one();
            if (feedOrder != null) {
                feedOrder.setStatus(2);
                iFeedOrderService.updateById(feedOrder);
                // 送东西（投喂订单支付成功）：岛宅进度 + 清洁度 + 活力值
                try {
                    islandCareService.applyGiftCare(feedOrder.getUserId());
                } catch (Exception e) {
                    log.warn("[支付回调] 岛宅送东西加成失败 orderNo={}", feedOrder.getOrderNo(), e);
                }
            }
        } else if (Objects.equals(payOrder.getBusinessType(), BusinessType.VIP.getCode())) {
            log.info("[支付回调] VIP业务待接入 payNo={}", outTradeNo);
        }
        log.info("[支付回调] 成功 payNo={} tradeNo={}", outTradeNo, tradeNo);
    }

    @Override
    public void onPaymentFailed(String channel, String outTradeNo, String tradeNo) {
        payCallbackLogService.logCallback(outTradeNo, channel, "fail", tradeNo, "fail", 2);
        PayOrder payOrder = iPayOrderService.lambdaQuery().eq(PayOrder::getPayNo, outTradeNo).one();
        if (payOrder != null && !Objects.equals(payOrder.getStatus(), 3)) {
            payOrder.setStatus(4);
            iPayOrderService.updateById(payOrder);
        }
        log.info("[支付回调] 失败 payNo={}", outTradeNo);
    }
}
