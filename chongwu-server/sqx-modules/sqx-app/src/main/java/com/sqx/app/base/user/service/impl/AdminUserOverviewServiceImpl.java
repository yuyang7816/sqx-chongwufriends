package com.sqx.app.base.user.service.impl;

import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.domain.vo.AdminUserOverviewVo;
import com.sqx.app.base.user.service.AdminUserOverviewService;
import com.sqx.app.pet.asset.db.service.IUserAssetService;
import com.sqx.app.pet.asset.domain.entity.UserAsset;
import com.sqx.app.pet.community.db.service.ICommunityReportService;
import com.sqx.app.pet.community.domain.entity.CommunityReport;
import com.sqx.app.pet.feed.db.service.IFeedOrderService;
import com.sqx.app.pet.feed.domain.entity.FeedOrder;
import com.sqx.app.pet.island.db.service.IIslandPetService;
import com.sqx.app.pet.island.domain.entity.IslandPet;
import com.sqx.app.pet.mall.db.service.IMallOrderService;
import com.sqx.app.pet.mall.db.service.IUserCouponService;
import com.sqx.app.pet.mall.domain.entity.MallOrder;
import com.sqx.app.pet.mall.domain.entity.UserCoupon;
import com.sqx.app.pet.marketing.db.service.IMarketingAssistService;
import com.sqx.app.pet.marketing.domain.entity.MarketingAssist;
import com.sqx.app.pet.payment.db.service.IPayOrderService;
import com.sqx.app.pet.payment.domain.entity.PayOrder;
import com.sqx.common.core.exception.BizException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class AdminUserOverviewServiceImpl implements AdminUserOverviewService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private IUserService iUserService;
    @Resource
    private IUserAssetService iUserAssetService;
    @Resource
    private IMallOrderService iMallOrderService;
    @Resource
    private IFeedOrderService iFeedOrderService;
    @Resource
    private IPayOrderService iPayOrderService;
    @Resource
    private IUserCouponService iUserCouponService;
    @Resource
    private IIslandPetService iIslandPetService;
    @Resource
    private IMarketingAssistService iMarketingAssistService;
    @Resource
    private ICommunityReportService iCommunityReportService;

    @Override
    public AdminUserOverviewVo overview(Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (Objects.isNull(user)) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        AdminUserOverviewVo vo = new AdminUserOverviewVo();
        vo.setUserId(user.getUserId());
        vo.setUserName(user.getUserName());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setUserType(user.getUserType());
        vo.setIsVip(user.getIsVip());
        vo.setInvitationCode(user.getInvitationCode());
        vo.setSuperiorInvitationCode(user.getSuperiorInvitationCode());
        vo.setPlatform(user.getPlatform());
        if (user.getCreateTime() != null) {
            vo.setCreateTime(user.getCreateTime().format(DT));
        }

        UserAsset asset = iUserAssetService.lambdaQuery().eq(UserAsset::getUserId, userId).one();
        if (asset != null) {
            vo.setPoints(asset.getPoints());
            vo.setCoins(asset.getCoins());
            vo.setFlowers(asset.getFlowers());
            vo.setLoveValue(asset.getLoveValue());
            vo.setLevel(asset.getLevel());
            vo.setLevelName(asset.getLevelName());
        } else {
            vo.setPoints(0);
            vo.setCoins(0);
            vo.setFlowers(0);
            vo.setLoveValue(0);
        }

        List<MallOrder> mallOrders = iMallOrderService.lambdaQuery()
                .eq(MallOrder::getUserId, userId)
                .ne(MallOrder::getBizType, 4)
                .list();
        vo.setMallOrderCount(mallOrders.size());
        vo.setMallOrderAmount(mallOrders.stream()
                .map(MallOrder::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        List<FeedOrder> feedOrders = iFeedOrderService.lambdaQuery()
                .eq(FeedOrder::getUserId, userId)
                .list();
        vo.setFeedOrderCount(feedOrders.size());
        vo.setFeedOrderAmount(feedOrders.stream()
                .map(FeedOrder::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        vo.setGiftPendingFeedback((int) feedOrders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() != 3)
                .count());

        vo.setPendingPayCount(iPayOrderService.lambdaQuery()
                .eq(PayOrder::getUserId, userId)
                .eq(PayOrder::getStatus, 1)
                .count()
                .intValue());

        LocalDateTime now = LocalDateTime.now();
        vo.setCouponAvailable(iUserCouponService.lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 1)
                .and(qw -> qw.isNull(UserCoupon::getExpireTime).or().gt(UserCoupon::getExpireTime, now))
                .count()
                .intValue());

        vo.setIslandPetCount(iIslandPetService.lambdaQuery()
                .eq(IslandPet::getUserId, userId)
                .count()
                .intValue());

        vo.setAssistCount(iMarketingAssistService.lambdaQuery()
                .eq(MarketingAssist::getUserId, userId)
                .count()
                .intValue());

        vo.setExchangePending(iMallOrderService.lambdaQuery()
                .eq(MallOrder::getUserId, userId)
                .eq(MallOrder::getBizType, 4)
                .in(MallOrder::getStatus, 1, 2)
                .count()
                .intValue());

        vo.setComplaintCount(iCommunityReportService.lambdaQuery()
                .eq(CommunityReport::getUserId, userId)
                .count()
                .intValue());

        return vo;
    }
}
