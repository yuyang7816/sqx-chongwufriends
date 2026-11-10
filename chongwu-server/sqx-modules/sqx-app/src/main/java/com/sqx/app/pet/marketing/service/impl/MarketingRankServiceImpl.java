package com.sqx.app.pet.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.archive.db.service.IPetArchiveService;
import com.sqx.app.pet.archive.domain.entity.PetArchive;
import com.sqx.app.pet.asset.db.service.IUserAssetService;
import com.sqx.app.pet.asset.domain.entity.UserAsset;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.common.service.AssetLedgerService;
import com.sqx.app.pet.island.service.IslandCareService;
import com.sqx.app.pet.marketing.db.service.IMarketingAssistService;
import com.sqx.app.pet.marketing.db.service.IMarketingRankPeriodService;
import com.sqx.app.pet.marketing.db.service.IMarketingRankRecordService;
import com.sqx.app.pet.marketing.domain.bo.AssistPageBo;
import com.sqx.app.pet.marketing.domain.bo.RankingAssistBo;
import com.sqx.app.pet.marketing.domain.bo.RankingAssistSettingsBo;
import com.sqx.app.pet.marketing.domain.bo.RankingPageBo;
import com.sqx.app.pet.marketing.domain.entity.MarketingAssist;
import com.sqx.app.pet.marketing.domain.entity.MarketingRankPeriod;
import com.sqx.app.pet.marketing.domain.entity.MarketingRankRecord;
import com.sqx.app.pet.marketing.domain.vo.AssistSummaryVo;
import com.sqx.app.pet.marketing.domain.vo.MarketingAssistVo;
import com.sqx.app.pet.marketing.domain.vo.MarketingRankPeriodVo;
import com.sqx.app.pet.marketing.domain.vo.MarketingRankVo;
import com.sqx.app.pet.marketing.domain.vo.RankingAssistQuotaVo;
import com.sqx.app.pet.marketing.domain.vo.RankingAssistSettingsVo;
import com.sqx.app.pet.marketing.domain.vo.RankingPeriodOverviewVo;
import com.sqx.app.pet.marketing.service.MarketingRankService;
import com.sqx.app.pet.store.db.service.IPetStoreService;
import com.sqx.app.pet.store.domain.entity.PetStore;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.common.web.utils.HttpContextUtil;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.db.service.ISysConfigService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MarketingRankServiceImpl implements MarketingRankService {

    private static final int DEFAULT_DAILY_FREE_LIMIT = 10;
    private static final int DEFAULT_POINTS_COST = 10;
    private static final int DEFAULT_COINS_COST = 10;
    private static final String RANKING_RULES_KEY = "ranking_rules";
    private static final String CFG_DAILY_FREE_LIMIT = "ranking_daily_free_limit";
    private static final String CFG_POINTS_COST = "ranking_assist_points_cost";
    private static final String CFG_COINS_COST = "ranking_assist_coins_cost";
    private static final String PAY_POINTS = "points";
    private static final String PAY_COINS = "coins";
    private static final String DEFAULT_RANKING_RULES =
            "每周一 0 点重置榜单；每日有免费打榜次数，用完后可用积分或金币继续打榜；每月 TOP3 宠物将获得专属周边礼包。";

    @Resource
    private IMarketingRankPeriodService iMarketingRankPeriodService;
    @Resource
    private IMarketingRankRecordService iMarketingRankRecordService;
    @Resource
    private IMarketingAssistService iMarketingAssistService;
    @Resource
    private IPetArchiveService iPetArchiveService;
    @Resource
    private IPetStoreService iPetStoreService;
    @Resource
    private ISysConfigService iSysConfigService;
    @Resource
    private AssetLedgerService assetLedgerService;
    @Resource
    private IUserAssetService iUserAssetService;
    @Resource
    private IslandCareService islandCareService;

    @Override
    public IPage<MarketingRankVo> currentRanking(RankingPageBo bo) {
        Long periodId = resolvePeriodId(bo);
        Page<MarketingRankRecord> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<MarketingRankRecord> qw = new QueryWrapper<>();
        qw.lambda().eq(MarketingRankRecord::getPeriodId, periodId).orderByAsc(MarketingRankRecord::getRankNo);
        IPage<MarketingRankRecord> iPage = iMarketingRankRecordService.page(page, qw);
        List<MarketingRankVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (MarketingRankRecord record : iPage.getRecords()) {
                voList.add(toRankVo(record));
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assist(Long userId, RankingAssistBo bo) {
        Long periodId = bo.getPeriodId() != null ? bo.getPeriodId() : resolveWeekPeriodId(0);
        RankingAssistSettingsVo settings = getAssistSettings();
        int freeLimit = settings.getDailyFreeLimit() == null ? DEFAULT_DAILY_FREE_LIMIT : settings.getDailyFreeLimit();
        long todayCount = countTodayAssists(userId);
        boolean freeLeft = todayCount < freeLimit;

        String assistType;
        if (freeLeft) {
            assistType = StringUtils.defaultIfBlank(bo.getAssistType(), "like");
        } else {
            String payType = StringUtils.trimToEmpty(bo.getPayType()).toLowerCase();
            if (StringUtils.isBlank(payType) && StringUtils.isNotBlank(bo.getAssistType())) {
                payType = bo.getAssistType().trim().toLowerCase();
            }
            if (StringUtils.isBlank(payType)) {
                throw new BizException(PetError.ASSIST_PAY_TYPE_REQUIRED);
            }
            if (!PAY_POINTS.equals(payType) && !PAY_COINS.equals(payType)) {
                throw new BizException(PetError.ASSIST_PAY_TYPE_INVALID);
            }
            int cost = PAY_POINTS.equals(payType)
                    ? (settings.getPointsCost() == null ? DEFAULT_POINTS_COST : settings.getPointsCost())
                    : (settings.getCoinsCost() == null ? DEFAULT_COINS_COST : settings.getCoinsCost());
            if (cost <= 0) {
                throw new BizException(PetError.ASSIST_PAY_COST_INVALID);
            }
            String assetType = PAY_POINTS.equals(payType) ? AssetLedgerService.POINTS : AssetLedgerService.COINS;
            String title = PAY_POINTS.equals(payType) ? "积分打榜" : "金币打榜";
            assetLedgerService.change(userId, assetType, cost, 2, "ranking_assist", bo.getPetId(), title);
            assistType = payType;
        }

        String idempotentKey = resolveIdempotentKey();
        if (idempotentKey != null) {
            boolean dup = iMarketingAssistService.lambdaQuery()
                    .eq(MarketingAssist::getUserId, userId)
                    .eq(MarketingAssist::getIdempotentKey, idempotentKey)
                    .exists();
            if (dup) {
                throw new BizException(PetError.DUPLICATE_REQUEST);
            }
        }
        MarketingAssist assist = new MarketingAssist();
        assist.setPeriodId(periodId);
        assist.setPetId(bo.getPetId());
        assist.setUserId(userId);
        assist.setAssistType(assistType);
        assist.setIdempotentKey(idempotentKey);
        iMarketingAssistService.save(assist);
        MarketingRankRecord record = iMarketingRankRecordService.lambdaQuery()
                .eq(MarketingRankRecord::getPeriodId, periodId)
                .eq(MarketingRankRecord::getPetId, bo.getPetId())
                .one();
        if (record == null) {
            record = new MarketingRankRecord();
            record.setPeriodId(periodId);
            record.setPetId(bo.getPetId());
            record.setLikeCount(1);
            record.setRankNo(9999);
            iMarketingRankRecordService.save(record);
        } else {
            record.setLikeCount((record.getLikeCount() == null ? 0 : record.getLikeCount()) + 1);
            iMarketingRankRecordService.updateById(record);
        }
        refreshPeriodRanks(periodId);
        PetArchive pet = iPetArchiveService.getById(bo.getPetId());
        if (pet != null) {
            pet.setLikeCount((pet.getLikeCount() == null ? 0 : pet.getLikeCount()) + 1);
            iPetArchiveService.updateById(pet);
        }
        // 打榜成功：岛宅今日进度 + 清洁度 + 活力值
        try {
            islandCareService.applyRankingCare(userId);
        } catch (Exception ignore) {
        }
    }

    @Override
    public List<MarketingAssistVo> myAssistRecords(Long userId) {
        List<MarketingAssist> list = iMarketingAssistService.lambdaQuery()
                .eq(MarketingAssist::getUserId, userId)
                .orderByDesc(MarketingAssist::getCreateTime)
                .last("limit 50")
                .list();
        List<MarketingAssistVo> voList = new ArrayList<>();
        for (MarketingAssist assist : list) {
            voList.add(toAssistVo(assist));
        }
        return voList;
    }

    @Override
    public IPage<MarketingAssistVo> myAssistPage(Long userId, AssistPageBo bo) {
        Page<MarketingAssist> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<MarketingAssist> qw = new QueryWrapper<>();
        boolean received = "received".equalsIgnoreCase(bo.getDirection());
        if (received) {
            // 助力我的：其他用户对我助力过的宠物进行助力
            List<Long> myPetIds = iMarketingAssistService.lambdaQuery()
                    .eq(MarketingAssist::getUserId, userId)
                    .select(MarketingAssist::getPetId)
                    .list()
                    .stream()
                    .map(MarketingAssist::getPetId)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            if (myPetIds.isEmpty()) {
                return PageUtil.copyPage(page, new ArrayList<>());
            }
            qw.lambda()
                    .in(MarketingAssist::getPetId, myPetIds)
                    .ne(MarketingAssist::getUserId, userId)
                    .orderByDesc(MarketingAssist::getCreateTime);
        } else {
            qw.lambda()
                    .eq(MarketingAssist::getUserId, userId)
                    .orderByDesc(MarketingAssist::getCreateTime);
        }
        IPage<MarketingAssist> iPage = iMarketingAssistService.page(page, qw);
        List<MarketingAssistVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (MarketingAssist assist : iPage.getRecords()) {
                voList.add(toAssistVo(assist));
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public AssistSummaryVo assistSummary(Long userId) {
        AssistSummaryVo summary = new AssistSummaryVo();
        int totalCount = Math.toIntExact(iMarketingAssistService.lambdaQuery()
                .eq(MarketingAssist::getUserId, userId)
                .count());
        summary.setTotalCount(totalCount);
        List<Long> myPetIds = iMarketingAssistService.lambdaQuery()
                .eq(MarketingAssist::getUserId, userId)
                .select(MarketingAssist::getPetId)
                .list()
                .stream()
                .map(MarketingAssist::getPetId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        int receivedCount = 0;
        if (!myPetIds.isEmpty()) {
            receivedCount = Math.toIntExact(iMarketingAssistService.lambdaQuery()
                    .in(MarketingAssist::getPetId, myPetIds)
                    .ne(MarketingAssist::getUserId, userId)
                    .count());
        }
        summary.setReceivedCount(receivedCount);
        return summary;
    }

    @Override
    public RankingAssistQuotaVo assistQuota(Long userId) {
        RankingAssistSettingsVo settings = getAssistSettings();
        int freeLimit = settings.getDailyFreeLimit() == null ? DEFAULT_DAILY_FREE_LIMIT : settings.getDailyFreeLimit();
        int todayUsed = Math.toIntExact(countTodayAssists(userId));
        RankingAssistQuotaVo quota = new RankingAssistQuotaVo();
        quota.setDailyFreeLimit(freeLimit);
        quota.setTodayUsed(todayUsed);
        quota.setFreeLeft(Math.max(freeLimit - todayUsed, 0));
        quota.setPointsCost(settings.getPointsCost());
        quota.setCoinsCost(settings.getCoinsCost());
        UserAsset asset = iUserAssetService.lambdaQuery().eq(UserAsset::getUserId, userId).one();
        quota.setPoints(asset == null || asset.getPoints() == null ? 0 : asset.getPoints());
        quota.setCoins(asset == null || asset.getCoins() == null ? 0 : asset.getCoins());
        return quota;
    }

    @Override
    public RankingPeriodOverviewVo periodOverview() {
        RankingPeriodOverviewVo overview = new RankingPeriodOverviewVo();
        overview.setCurrentWeek(toPeriodVo(resolveWeekPeriod(0), 0));
        overview.setPreviousWeek(toPeriodVo(resolveWeekPeriod(-1), -1));
        overview.setRules(getRankingRules());
        overview.setAssistSettings(getAssistSettings());
        return overview;
    }

    @Override
    public RankingAssistSettingsVo getAssistSettings() {
        RankingAssistSettingsVo vo = new RankingAssistSettingsVo();
        vo.setDailyFreeLimit(getConfigInt(CFG_DAILY_FREE_LIMIT, DEFAULT_DAILY_FREE_LIMIT));
        vo.setPointsCost(getConfigInt(CFG_POINTS_COST, DEFAULT_POINTS_COST));
        vo.setCoinsCost(getConfigInt(CFG_COINS_COST, DEFAULT_COINS_COST));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssistSettings(RankingAssistSettingsBo bo) {
        upsertSysConfig(CFG_DAILY_FREE_LIMIT, String.valueOf(bo.getDailyFreeLimit()), "每日免费打榜次数");
        upsertSysConfig(CFG_POINTS_COST, String.valueOf(bo.getPointsCost()), "付费打榜积分消耗");
        upsertSysConfig(CFG_COINS_COST, String.valueOf(bo.getCoinsCost()), "付费打榜金币消耗");
        iSysConfigService.loadSysConfig();
    }

    @Override
    public String getRankingRules() {
        SysConfigEntity config = iSysConfigService.getOne(new LambdaQueryWrapper<SysConfigEntity>()
                .eq(SysConfigEntity::getParamKey, RANKING_RULES_KEY)
                .last("limit 1"));
        if (config != null && StringUtils.isNotBlank(config.getParamValue())) {
            return config.getParamValue().trim();
        }
        return DEFAULT_RANKING_RULES;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRankingRules(String rules) {
        SysConfigEntity entity = iSysConfigService.getOne(new LambdaQueryWrapper<SysConfigEntity>()
                .eq(SysConfigEntity::getParamKey, RANKING_RULES_KEY)
                .last("limit 1"));
        if (entity == null) {
            entity = new SysConfigEntity();
            entity.setParamKey(RANKING_RULES_KEY);
            entity.setParamType("marketing");
            entity.setParamTypeDesc("打榜运营");
            entity.setStatus("1");
            entity.setRemark("打榜规则说明");
        }
        entity.setParamValue(rules.trim());
        if (entity.getId() == null) {
            iSysConfigService.save(entity);
        } else {
            iSysConfigService.updateById(entity);
        }
        iSysConfigService.loadSysConfig();
    }

    private Long resolvePeriodId(RankingPageBo bo) {
        if (bo.getPeriodId() != null) {
            return bo.getPeriodId();
        }
        int weekOffset = bo.getWeekOffset() != null ? bo.getWeekOffset() : 0;
        return resolveWeekPeriodId(weekOffset);
    }

    private MarketingRankVo toRankVo(MarketingRankRecord record) {
        MarketingRankVo vo = new MarketingRankVo();
        vo.setPetId(record.getPetId());
        vo.setLikeCount(record.getLikeCount());
        vo.setRankNo(record.getRankNo());
        PetArchive pet = iPetArchiveService.getById(record.getPetId());
        if (pet != null) {
            vo.setPetName(pet.getName());
            vo.setAvatarUrl(pet.getAvatarUrl());
            if (pet.getStoreId() != null) {
                PetStore store = iPetStoreService.getById(pet.getStoreId());
                if (store != null) {
                    vo.setStoreName(store.getName());
                }
            }
        }
        return vo;
    }

    private MarketingAssistVo toAssistVo(MarketingAssist assist) {
        MarketingAssistVo vo = new MarketingAssistVo();
        BeanUtils.copyProperties(assist, vo);
        PetArchive pet = iPetArchiveService.getById(assist.getPetId());
        if (pet != null) {
            vo.setPetName(pet.getName());
            vo.setTarget(pet.getName());
        }
        vo.setType(resolveAssistTypeText(assist.getAssistType()));
        vo.setStatus("成功");
        vo.setTime(formatAssistTime(assist.getCreateTime()));
        return vo;
    }

    private String resolveAssistTypeText(String assistType) {
        if ("points".equalsIgnoreCase(assistType)) {
            return "积分打榜";
        }
        if ("coins".equalsIgnoreCase(assistType)) {
            return "金币打榜";
        }
        if ("like".equalsIgnoreCase(assistType) || "free".equalsIgnoreCase(assistType)) {
            return "免费打榜";
        }
        return "助力";
    }

    private long countTodayAssists(Long userId) {
        LocalDateTime dayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime dayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return iMarketingAssistService.lambdaQuery()
                .eq(MarketingAssist::getUserId, userId)
                .between(MarketingAssist::getCreateTime, dayStart, dayEnd)
                .count();
    }

    private int getConfigInt(String key, int defaultVal) {
        SysConfigEntity config = iSysConfigService.getOne(new LambdaQueryWrapper<SysConfigEntity>()
                .eq(SysConfigEntity::getParamKey, key)
                .last("limit 1"));
        if (config == null || StringUtils.isBlank(config.getParamValue())) {
            return defaultVal;
        }
        try {
            return Math.max(0, Integer.parseInt(config.getParamValue().trim()));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private void upsertSysConfig(String key, String value, String remark) {
        SysConfigEntity entity = iSysConfigService.getOne(new LambdaQueryWrapper<SysConfigEntity>()
                .eq(SysConfigEntity::getParamKey, key)
                .last("limit 1"));
        if (entity == null) {
            entity = new SysConfigEntity();
            entity.setParamKey(key);
            entity.setParamType("marketing");
            entity.setParamTypeDesc("打榜运营");
            entity.setStatus("1");
            entity.setRemark(remark);
            entity.setParamValue(value);
            iSysConfigService.save(entity);
            return;
        }
        entity.setParamValue(value);
        entity.setRemark(remark);
        iSysConfigService.updateById(entity);
    }

    private String formatAssistTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        LocalDate today = LocalDate.now();
        if (time.toLocalDate().equals(today)) {
            return "今天 " + time.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        if (time.toLocalDate().equals(today.minusDays(1))) {
            return "昨天 " + time.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return time.format(DateTimeFormatter.ofPattern("MM月dd日 HH:mm"));
    }

    private MarketingRankPeriod resolveWeekPeriod(int weekOffset) {
        LocalDate monday = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .plusWeeks(weekOffset);
        LocalDateTime start = monday.atStartOfDay();
        LocalDateTime end = monday.plusDays(6).atTime(23, 59, 59);
        MarketingRankPeriod period = iMarketingRankPeriodService.lambdaQuery()
                .eq(MarketingRankPeriod::getStatus, 1)
                .eq(MarketingRankPeriod::getStartTime, start)
                .one();
        if (period != null) {
            return period;
        }
        period = new MarketingRankPeriod();
        period.setName(monday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 人气榜");
        period.setStartTime(start);
        period.setEndTime(end);
        period.setStatus(1);
        iMarketingRankPeriodService.save(period);
        seedPeriodRecords(period.getId(), weekOffset);
        return period;
    }

    private Long resolveWeekPeriodId(int weekOffset) {
        return resolveWeekPeriod(weekOffset).getId();
    }

    private void seedPeriodRecords(Long periodId, int weekOffset) {
        if (weekOffset == 0) {
            List<PetArchive> pets = iPetArchiveService.lambdaQuery()
                    .eq(PetArchive::getAuditStatus, 1)
                    .orderByDesc(PetArchive::getLikeCount)
                    .last("limit 10")
                    .list();
            int rank = 1;
            for (PetArchive pet : pets) {
                MarketingRankRecord record = new MarketingRankRecord();
                record.setPeriodId(periodId);
                record.setPetId(pet.getId());
                record.setLikeCount(Math.max(pet.getLikeCount() == null ? 0 : pet.getLikeCount(), rank * 100));
                record.setRankNo(rank++);
                iMarketingRankRecordService.save(record);
            }
            return;
        }
        Long currentPeriodId = resolveWeekPeriodId(0);
        List<MarketingRankRecord> currentRecords = iMarketingRankRecordService.lambdaQuery()
                .eq(MarketingRankRecord::getPeriodId, currentPeriodId)
                .orderByAsc(MarketingRankRecord::getRankNo)
                .list();
        for (MarketingRankRecord current : currentRecords) {
            MarketingRankRecord record = new MarketingRankRecord();
            record.setPeriodId(periodId);
            record.setPetId(current.getPetId());
            int base = current.getLikeCount() == null ? 0 : current.getLikeCount();
            record.setLikeCount(Math.max(base - 800, 1000));
            record.setRankNo(current.getRankNo());
            iMarketingRankRecordService.save(record);
        }
    }

    private void refreshPeriodRanks(Long periodId) {
        List<MarketingRankRecord> records = iMarketingRankRecordService.lambdaQuery()
                .eq(MarketingRankRecord::getPeriodId, periodId)
                .list();
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        records.sort(Comparator
                .comparing((MarketingRankRecord item) -> item.getLikeCount() == null ? 0 : item.getLikeCount())
                .reversed()
                .thenComparing(MarketingRankRecord::getPetId));
        int rank = 1;
        for (MarketingRankRecord record : records) {
            record.setRankNo(rank++);
            iMarketingRankRecordService.updateById(record);
        }
    }

    private MarketingRankPeriodVo toPeriodVo(MarketingRankPeriod period, int weekOffset) {
        MarketingRankPeriodVo vo = new MarketingRankPeriodVo();
        vo.setId(period.getId());
        vo.setName(period.getName());
        vo.setWeekOffset(weekOffset);
        vo.setStartTime(period.getStartTime());
        vo.setEndTime(period.getEndTime());
        return vo;
    }

    private String resolveIdempotentKey() {
        try {
            HttpServletRequest request = HttpContextUtil.getHttpServletRequest();
            return request.getHeader("Idempotent-Key");
        } catch (Exception e) {
            return null;
        }
    }
}
