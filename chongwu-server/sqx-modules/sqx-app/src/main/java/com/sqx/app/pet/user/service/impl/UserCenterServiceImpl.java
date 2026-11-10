package com.sqx.app.pet.user.service.impl;

import com.sqx.app.pet.feed.db.service.IFeedOrderService;
import com.sqx.app.pet.feed.domain.entity.FeedOrder;
import com.sqx.app.pet.mall.db.service.IMallOrderService;
import com.sqx.app.pet.mall.db.service.IUserCouponService;
import com.sqx.app.pet.mall.domain.entity.MallOrder;
import com.sqx.app.pet.mall.domain.entity.UserCoupon;
import com.sqx.app.pet.marketing.db.service.IMarketingAssistService;
import com.sqx.app.pet.marketing.domain.entity.MarketingAssist;
import com.sqx.app.pet.user.domain.vo.UserCenterSummaryVo;
import com.sqx.app.pet.user.service.UserCenterService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class UserCenterServiceImpl implements UserCenterService {

    @Resource
    private IUserCouponService iUserCouponService;
    @Resource
    private IFeedOrderService iFeedOrderService;
    @Resource
    private IMarketingAssistService iMarketingAssistService;
    @Resource
    private IMallOrderService iMallOrderService;

    @Override
    public UserCenterSummaryVo getSummary(Long userId) {
        UserCenterSummaryVo vo = new UserCenterSummaryVo();
        vo.setCouponAvailable(countAvailableCoupons(userId));
        vo.setGiftPendingFeedback(countPendingGiftFeedback(userId));
        vo.setAssistCount(countAssist(userId));
        vo.setExchangePending(countPendingExchange(userId));
        return vo;
    }

    private int countAvailableCoupons(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return iUserCouponService.lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 1)
                .and(qw -> qw.isNull(UserCoupon::getExpireTime).or().gt(UserCoupon::getExpireTime, now))
                .count()
                .intValue();
    }

    private int countPendingGiftFeedback(Long userId) {
        return iFeedOrderService.lambdaQuery()
                .eq(FeedOrder::getUserId, userId)
                .ne(FeedOrder::getStatus, 3)
                .count()
                .intValue();
    }

    private int countAssist(Long userId) {
        return iMarketingAssistService.lambdaQuery()
                .eq(MarketingAssist::getUserId, userId)
                .count()
                .intValue();
    }

    private int countPendingExchange(Long userId) {
        return iMallOrderService.lambdaQuery()
                .eq(MallOrder::getUserId, userId)
                .eq(MallOrder::getBizType, 4)
                .in(MallOrder::getStatus, 1, 2)
                .count()
                .intValue();
    }
}
