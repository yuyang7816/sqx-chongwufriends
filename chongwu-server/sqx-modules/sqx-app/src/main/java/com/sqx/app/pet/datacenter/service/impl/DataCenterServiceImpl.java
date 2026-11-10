package com.sqx.app.pet.datacenter.service.impl;

import com.sqx.app.base.pay.enums.BusinessType;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.pet.adopt.db.service.IAdoptApplicationService;
import com.sqx.app.pet.adopt.domain.entity.AdoptApplication;
import com.sqx.app.pet.archive.db.service.IPetArchiveService;
import com.sqx.app.pet.community.db.service.ICommunityPostService;
import com.sqx.app.pet.community.db.service.ICommunityReportService;
import com.sqx.app.pet.community.domain.entity.CommunityPost;
import com.sqx.app.pet.community.domain.entity.CommunityReport;
import com.sqx.app.pet.datacenter.domain.vo.DataCenterOverviewVo;
import com.sqx.app.pet.datacenter.domain.vo.DataCenterTrendDayVo;
import com.sqx.app.pet.datacenter.service.DataCenterService;
import com.sqx.app.pet.feed.db.service.IFeedOrderService;
import com.sqx.app.pet.feed.domain.entity.FeedOrder;
import com.sqx.app.pet.mall.db.service.IMallSpuService;
import com.sqx.app.pet.mall.db.service.IUserCouponService;
import com.sqx.app.pet.mall.domain.entity.MallSpu;
import com.sqx.app.pet.mall.domain.entity.UserCoupon;
import com.sqx.app.pet.payment.db.service.IPayOrderService;
import com.sqx.app.pet.payment.domain.entity.PayOrder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DataCenterServiceImpl implements DataCenterService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 支付成功 */
    private static final int PAY_SUCCESS = 3;
    /** 投喂已支付待反馈 */
    private static final int FEED_PAID = 2;

    @Resource
    private IPayOrderService iPayOrderService;
    @Resource
    private IFeedOrderService iFeedOrderService;
    @Resource
    private IUserService iUserService;
    @Resource
    private ICommunityPostService iCommunityPostService;
    @Resource
    private ICommunityReportService iCommunityReportService;
    @Resource
    private IAdoptApplicationService iAdoptApplicationService;
    @Resource
    private IUserCouponService iUserCouponService;
    @Resource
    private IPetArchiveService iPetArchiveService;
    @Resource
    private IMallSpuService iMallSpuService;

    @Override
    public DataCenterOverviewVo overview() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate trendStart = today.minusDays(6);
        LocalDateTime trendStartAt = trendStart.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<PayOrder> paidOrders = iPayOrderService.lambdaQuery()
                .eq(PayOrder::getStatus, PAY_SUCCESS)
                .isNotNull(PayOrder::getPayTime)
                .ge(PayOrder::getPayTime, trendStartAt)
                .lt(PayOrder::getPayTime, tomorrowStart)
                .select(PayOrder::getAmount, PayOrder::getPayTime, PayOrder::getBusinessType)
                .list();

        List<FeedOrder> feedOrders = iFeedOrderService.lambdaQuery()
                .ge(FeedOrder::getStatus, FEED_PAID)
                .ge(FeedOrder::getCreateTime, trendStartAt)
                .lt(FeedOrder::getCreateTime, tomorrowStart)
                .select(FeedOrder::getAmount, FeedOrder::getCreateTime, FeedOrder::getStatus)
                .list();

        List<UserEntity> newUsers = iUserService.lambdaQuery()
                .ge(UserEntity::getCreateTime, trendStartAt)
                .lt(UserEntity::getCreateTime, tomorrowStart)
                .select(UserEntity::getCreateTime)
                .list();

        Map<String, DayBucket> buckets = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = trendStart.plusDays(i);
            buckets.put(d.format(DAY), new DayBucket());
        }

        for (PayOrder po : paidOrders) {
            if (po.getPayTime() == null) {
                continue;
            }
            DayBucket b = buckets.get(po.getPayTime().toLocalDate().format(DAY));
            if (b == null) {
                continue;
            }
            BigDecimal amt = po.getAmount() == null ? BigDecimal.ZERO : po.getAmount();
            b.revenue = b.revenue.add(amt);
            if (Objects.equals(po.getBusinessType(), BusinessType.MALL_ORDER.getCode())) {
                b.mallCount++;
                b.mallAmount = b.mallAmount.add(amt);
            } else if (Objects.equals(po.getBusinessType(), BusinessType.FEED_ORDER.getCode())) {
                b.feedCount++;
                b.feedAmount = b.feedAmount.add(amt);
            }
        }

        // 投喂表作兜底（无支付单或历史数据），按日去重累加：若当日已有支付投喂金额则不再用 feed_order 覆盖
        Map<String, BigDecimal> feedFallback = new HashMap<>();
        Map<String, Long> feedFallbackCount = new HashMap<>();
        for (FeedOrder fo : feedOrders) {
            if (fo.getCreateTime() == null) {
                continue;
            }
            String key = fo.getCreateTime().toLocalDate().format(DAY);
            BigDecimal amt = fo.getAmount() == null ? BigDecimal.ZERO : fo.getAmount();
            feedFallback.merge(key, amt, BigDecimal::add);
            feedFallbackCount.merge(key, 1L, Long::sum);
        }
        for (Map.Entry<String, DayBucket> e : buckets.entrySet()) {
            DayBucket b = e.getValue();
            if (b.feedAmount.compareTo(BigDecimal.ZERO) == 0 && feedFallback.containsKey(e.getKey())) {
                b.feedAmount = feedFallback.get(e.getKey());
                b.feedCount = feedFallbackCount.getOrDefault(e.getKey(), 0L);
                b.revenue = b.revenue.add(b.feedAmount);
            }
        }

        for (UserEntity u : newUsers) {
            if (u.getCreateTime() == null) {
                continue;
            }
            DayBucket b = buckets.get(u.getCreateTime().toLocalDate().format(DAY));
            if (b != null) {
                b.newUsers++;
            }
        }

        String todayKey = today.format(DAY);
        String yesterdayKey = yesterday.format(DAY);
        DayBucket todayB = buckets.getOrDefault(todayKey, new DayBucket());
        DayBucket yesterdayB = buckets.getOrDefault(yesterdayKey, new DayBucket());

        DataCenterOverviewVo vo = new DataCenterOverviewVo();
        vo.setTodayRevenue(todayB.revenue);
        vo.setYesterdayRevenue(yesterdayB.revenue);
        vo.setTodayMallOrderCount(todayB.mallCount);
        vo.setYesterdayMallOrderCount(yesterdayB.mallCount);
        vo.setTodayMallAmount(todayB.mallAmount);
        vo.setTodayAvgOrderAmount(avg(todayB.mallAmount, todayB.mallCount));
        vo.setTodayFeedCount(todayB.feedCount);
        vo.setYesterdayFeedCount(yesterdayB.feedCount);
        vo.setTodayFeedAmount(todayB.feedAmount);
        vo.setYesterdayFeedAmount(yesterdayB.feedAmount);
        vo.setTodayNewUserCount(todayB.newUsers);
        vo.setYesterdayNewUserCount(yesterdayB.newUsers);

        vo.setCommunityPending(iCommunityPostService.lambdaQuery()
                .eq(CommunityPost::getAuditStatus, 0)
                .count());
        vo.setAdoptPending(iAdoptApplicationService.lambdaQuery()
                .ge(AdoptApplication::getStatus, 1)
                .le(AdoptApplication::getStatus, 5)
                .count());
        vo.setFeedPending(iFeedOrderService.lambdaQuery()
                .eq(FeedOrder::getStatus, FEED_PAID)
                .count());
        vo.setComplaintPending(iCommunityReportService.lambdaQuery()
                .eq(CommunityReport::getStatus, 1)
                .count());
        vo.setCouponExpiring(iUserCouponService.lambdaQuery()
                .eq(UserCoupon::getStatus, 1)
                .isNotNull(UserCoupon::getExpireTime)
                .ge(UserCoupon::getExpireTime, now)
                .le(UserCoupon::getExpireTime, now.plusDays(7))
                .count());

        LocalDateTime todayStart = today.atStartOfDay();
        vo.setCommunityTotal(iCommunityPostService.lambdaQuery().count());
        vo.setCommunityToday(iCommunityPostService.lambdaQuery()
                .ge(CommunityPost::getCreateTime, todayStart)
                .lt(CommunityPost::getCreateTime, tomorrowStart)
                .count());
        vo.setArchiveCount(iPetArchiveService.lambdaQuery().count());
        vo.setMallOnCount(iMallSpuService.lambdaQuery()
                .eq(MallSpu::getStatus, 1)
                .count());

        List<DataCenterTrendDayVo> trend = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate d = trendStart.plusDays(i);
            String key = d.format(DAY);
            DayBucket b = buckets.getOrDefault(key, new DayBucket());
            DataCenterTrendDayVo point = new DataCenterTrendDayVo();
            point.setDate(key);
            point.setRevenue(b.revenue);
            point.setMallOrderCount(b.mallCount);
            point.setFeedAmount(b.feedAmount);
            point.setNewUserCount(b.newUsers);
            trend.add(point);
        }
        vo.setTrend7d(trend);
        return vo;
    }

    private static BigDecimal avg(BigDecimal amount, long count) {
        if (count <= 0 || amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private static class DayBucket {
        private BigDecimal revenue = BigDecimal.ZERO;
        private BigDecimal mallAmount = BigDecimal.ZERO;
        private BigDecimal feedAmount = BigDecimal.ZERO;
        private long mallCount;
        private long feedCount;
        private long newUsers;
    }
}
