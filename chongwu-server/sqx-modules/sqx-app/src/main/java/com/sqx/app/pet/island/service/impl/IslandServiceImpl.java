package com.sqx.app.pet.island.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.archive.db.service.IPetArchiveService;
import com.sqx.app.pet.archive.domain.entity.PetArchive;
import com.sqx.app.pet.archive.domain.vo.PetArchiveVo;
import com.sqx.app.pet.store.db.service.IPetStoreService;
import com.sqx.app.pet.store.domain.entity.PetStore;
import com.sqx.app.pet.asset.domain.vo.UserAssetVo;
import com.sqx.app.pet.asset.service.UserAssetService;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.common.service.AssetLedgerService;
import com.sqx.app.pet.island.db.service.IIslandMarketItemService;
import com.sqx.app.pet.island.db.service.IIslandMarketRecordService;
import com.sqx.app.pet.island.db.service.IIslandPetService;
import com.sqx.app.pet.island.domain.bo.IslandBuyMarketBo;
import com.sqx.app.pet.island.domain.bo.IslandFeedBo;
import com.sqx.app.pet.island.domain.bo.IslandMarketRecordPageBo;
import com.sqx.app.pet.island.domain.bo.IslandResidentSaveBo;
import com.sqx.app.pet.island.domain.entity.IslandMarketItem;
import com.sqx.app.pet.island.domain.entity.IslandMarketRecord;
import com.sqx.app.pet.island.domain.entity.IslandPet;
import com.sqx.app.pet.island.domain.vo.IslandActionVo;
import com.sqx.app.pet.island.domain.vo.IslandMarketItemVo;
import com.sqx.app.pet.island.domain.vo.IslandMarketRecordVo;
import com.sqx.app.pet.island.domain.vo.IslandPetVo;
import com.sqx.app.pet.island.domain.vo.IslandResidentVo;
import com.sqx.app.pet.island.service.IslandCareService;
import com.sqx.app.pet.island.service.IslandPetService;
import com.sqx.app.pet.island.service.IslandService;
import com.sqx.app.pet.island.service.IslandTaskService;
import com.sqx.app.pet.userpet.service.UserPetService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.common.web.utils.HttpContextUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IslandServiceImpl implements IslandService {

    @Resource
    private IIslandPetService iIslandPetService;
    @Resource
    private IslandPetService islandPetService;
    @Resource
    private IIslandMarketItemService iIslandMarketItemService;
    @Resource
    private IIslandMarketRecordService iIslandMarketRecordService;
    @Resource
    private AssetLedgerService assetLedgerService;
    @Resource
    private UserAssetService userAssetService;
    @Resource
    private IPetArchiveService iPetArchiveService;
    @Resource
    private IPetStoreService iPetStoreService;
    @Resource
    private IslandTaskService islandTaskService;
    @Resource
    private UserPetService userPetService;
    @Resource
    private IslandCareService islandCareService;

    @Override
    public IslandPetVo getMyIsland(Long userId) {
        islandPetService.initForUser(userId);
        IslandPet pet = iIslandPetService.lambdaQuery().eq(IslandPet::getUserId, userId).one();
        if (pet == null) {
            throw new BizException(PetError.ISLAND_PET_NOT_FOUND);
        }
        // 无常驻时默认展示用户第一只自有宠物
        ensureDefaultResidentIfNeeded(userId, pet);
        pet = iIslandPetService.lambdaQuery().eq(IslandPet::getUserId, userId).one();
        boolean dirty = refreshBowlCooldownState(pet);
        java.time.LocalDate beforeDate = pet.getStatDate();
        islandCareService.ensureDailyReset(pet);
        boolean reset = !Objects.equals(beforeDate, pet.getStatDate());
        if (dirty || reset) {
            saveIslandPet(pet);
        }
        IslandPetVo vo = new IslandPetVo();
        BeanUtils.copyProperties(pet, vo);
        enrichVo(pet, vo, userId);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IslandPetVo feed(Long userId, IslandFeedBo bo) {
        IslandPet pet = iIslandPetService.lambdaQuery().eq(IslandPet::getUserId, userId).one();
        if (pet == null) {
            throw new BizException(PetError.ISLAND_PET_NOT_FOUND);
        }
        refreshBowlCooldownState(pet);
        if (isCooling(pet)) {
            throw new BizException(PetError.FEED_COOLDOWN);
        }
        if (Boolean.TRUE.equals(bo.getUseAsset()) && bo.getAssetAmount() != null && bo.getAssetAmount() > 0) {
            String type = bo.getAssetType() != null ? bo.getAssetType() : AssetLedgerService.COINS;
            assetLedgerService.change(userId, type, bo.getAssetAmount(), 2, "island_feed", pet.getId(), "岛宅投喂");
        }
        String feedType = bo.getFeedType() == null ? "" : bo.getFeedType().trim().toLowerCase();
        boolean feedFood = "food".equals(feedType) || StringUtils.isBlank(feedType);
        boolean feedWater = "water".equals(feedType) || StringUtils.isBlank(feedType);
        if (feedFood && isBowlFilled(pet.getFoodBowlFilled())) {
            throw new BizException(PetError.FEED_BOWL_FILLED);
        }
        if (feedWater && isBowlFilled(pet.getWaterBowlFilled())) {
            throw new BizException(PetError.FEED_BOWL_FILLED);
        }
        islandCareService.ensureDailyReset(pet);
        if (feedFood) {
            islandCareService.applyFeedCare(pet);
            pet.setFoodBowlFilled(1);
        }
        if (feedWater) {
            islandCareService.applyFeedCare(pet);
            pet.setWaterBowlFilled(1);
        }
        int loveGain = resolveLoveGain(feedType);
        if (loveGain > 0) {
            String loveTitle = "water".equals(feedType) ? "岛宅喂水" : ("food".equals(feedType) ? "岛宅喂食" : "岛宅投喂");
            assetLedgerService.change(userId, AssetLedgerService.LOVE, loveGain, 1, "island_feed", pet.getId(), loveTitle);
        }
        // 饭水都喂完后才进入共用冷却；只喂一边仍可继续喂另一边
        if (isBowlFilled(pet.getFoodBowlFilled()) && isBowlFilled(pet.getWaterBowlFilled())) {
            pet.setFeedCooldownUntil(LocalDateTime.now().plusMinutes(islandCareService.feedCooldownMinutes()));
        }
        saveIslandPet(pet);
        islandTaskService.autoCompleteFeedTask(userId);
        IslandPetVo vo = new IslandPetVo();
        BeanUtils.copyProperties(pet, vo);
        enrichVo(pet, vo, userId);
        vo.setLoveGain(loveGain);
        vo.setLastFeedType(StringUtils.isBlank(feedType) ? "all" : feedType);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IslandPetVo buyMarketItem(Long userId, IslandBuyMarketBo bo) {
        IslandMarketItem item = iIslandMarketItemService.getById(bo.getItemId());
        if (item == null || !Objects.equals(item.getStatus(), 1)) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        String idempotentKey = resolveIdempotentKey();
        if (idempotentKey != null) {
            long dup = iIslandMarketRecordService.lambdaQuery()
                    .eq(IslandMarketRecord::getUserId, userId)
                    .eq(IslandMarketRecord::getIdempotentKey, idempotentKey)
                    .count();
            if (dup > 0) {
                throw new BizException(PetError.DUPLICATE_REQUEST);
            }
        }
        int priceCoins = item.getPriceCoins() == null ? 0 : item.getPriceCoins();
        int pricePoints = item.getPricePoints() == null ? 0 : item.getPricePoints();
        boolean canCoins = priceCoins > 0;
        boolean canPoints = pricePoints > 0;
        if (!canCoins && !canPoints) {
            throw new BizException("该道具暂未配置兑换价格");
        }
        String payType = resolveMarketPayType(bo.getPayType(), canCoins, canPoints);
        int payCoins = 0;
        int payPoints = 0;
        if (AssetLedgerService.COINS.equals(payType)) {
            payCoins = priceCoins;
            assetLedgerService.change(userId, AssetLedgerService.COINS, payCoins, 2, "island_market", item.getId(), "集市金币兑换");
        } else {
            payPoints = pricePoints;
            assetLedgerService.change(userId, AssetLedgerService.POINTS, payPoints, 2, "island_market", item.getId(), "集市积分兑换");
        }
        IslandPet pet = iIslandPetService.lambdaQuery().eq(IslandPet::getUserId, userId).one();
        if (pet != null) {
            int favor = item.getFavor() == null ? 0 : item.getFavor();
            if (favor > 0) {
                pet.setIntimacy((pet.getIntimacy() == null ? 0 : pet.getIntimacy()) + favor);
            }
            // 集市：进度 + 清洁度 + 活力值（配置化）
            islandCareService.applyMarketCare(pet);
            saveIslandPet(pet);
        }
        IslandMarketRecord record = new IslandMarketRecord();
        record.setUserId(userId);
        record.setItemId(item.getId());
        record.setPayCoins(payCoins);
        record.setPayPoints(payPoints);
        record.setFavor(item.getFavor() == null ? 0 : item.getFavor());
        record.setIdempotentKey(idempotentKey);
        iIslandMarketRecordService.save(record);
        try {
            islandTaskService.autoCompleteByAction(userId, "buy");
        } catch (Exception ignore) {
        }
        IslandPet updated = iIslandPetService.lambdaQuery().eq(IslandPet::getUserId, userId).one();
        IslandPetVo vo = new IslandPetVo();
        if (updated != null) {
            BeanUtils.copyProperties(updated, vo);
            enrichVo(updated, vo, userId);
        }
        return vo;
    }

    @Override
    public List<IslandMarketItemVo> listMarketItems() {
        QueryWrapper<IslandMarketItem> qw = new QueryWrapper<>();
        qw.lambda().eq(IslandMarketItem::getStatus, 1).orderByAsc(IslandMarketItem::getSort);
        List<IslandMarketItem> list = iIslandMarketItemService.list(qw);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(item -> {
            IslandMarketItemVo vo = new IslandMarketItemVo();
            BeanUtils.copyProperties(item, vo);
            int coins = item.getPriceCoins() == null ? 0 : item.getPriceCoins();
            int points = item.getPricePoints() == null ? 0 : item.getPricePoints();
            vo.setCanPayCoins(coins > 0);
            vo.setCanPayPoints(points > 0);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public IPage<IslandMarketRecordVo> listMarketRecords(Long userId, IslandMarketRecordPageBo bo) {
        Page<IslandMarketRecord> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<IslandMarketRecord> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(IslandMarketRecord::getUserId, userId)
                .orderByDesc(IslandMarketRecord::getCreateTime);
        IPage<IslandMarketRecord> iPage = iIslandMarketRecordService.page(page, qw);
        List<IslandMarketRecord> records = iPage.getRecords();
        if (records == null || records.isEmpty()) {
            return PageUtil.copyPage(iPage, Collections.emptyList());
        }
        Set<Long> itemIds = records.stream()
                .map(IslandMarketRecord::getItemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, IslandMarketItem> itemMap = new HashMap<>();
        if (!itemIds.isEmpty()) {
            List<IslandMarketItem> items = iIslandMarketItemService.listByIds(itemIds);
            if (items != null) {
                for (IslandMarketItem item : items) {
                    itemMap.put(item.getId(), item);
                }
            }
        }
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<IslandMarketRecordVo> voList = new ArrayList<>();
        for (IslandMarketRecord record : records) {
            IslandMarketRecordVo vo = new IslandMarketRecordVo();
            vo.setId(record.getId());
            vo.setItemId(record.getItemId());
            vo.setPayCoins(record.getPayCoins() == null ? 0 : record.getPayCoins());
            vo.setPayPoints(record.getPayPoints() == null ? 0 : record.getPayPoints());
            IslandMarketItem item = itemMap.get(record.getItemId());
            if (item != null) {
                vo.setName(item.getName());
                vo.setCoverUrl(item.getCoverUrl());
                vo.setItemType(item.getItemType());
            } else {
                vo.setName("集市道具");
            }
            // 优先用下单时快照的亲密度；旧数据无快照时回退到道具当前配置
            if (record.getFavor() != null) {
                vo.setFavor(record.getFavor());
            } else if (item != null) {
                vo.setFavor(item.getFavor() == null ? 0 : item.getFavor());
            } else {
                vo.setFavor(0);
            }
            if (record.getCreateTime() != null) {
                vo.setDate(record.getCreateTime().format(dateFmt));
                vo.setCreateTime(record.getCreateTime().format(timeFmt));
            } else {
                vo.setDate("");
                vo.setCreateTime("");
            }
            voList.add(vo);
        }
        return PageUtil.copyPage(iPage, voList);
    }

    /** 集市兑换：金币 / 积分二选一 */
    private String resolveMarketPayType(String raw, boolean canCoins, boolean canPoints) {
        String payType = raw == null ? "" : raw.trim().toLowerCase();
        if (AssetLedgerService.COINS.equals(payType) || "coin".equals(payType) || "金币".equals(payType)) {
            if (!canCoins) {
                throw new BizException("该道具不支持金币兑换");
            }
            return AssetLedgerService.COINS;
        }
        if (AssetLedgerService.POINTS.equals(payType) || "point".equals(payType) || "积分".equals(payType)) {
            if (!canPoints) {
                throw new BizException("该道具不支持积分兑换");
            }
            return AssetLedgerService.POINTS;
        }
        if (StringUtils.isNotBlank(payType)) {
            throw new BizException("支付方式无效，请选择金币或积分");
        }
        // 未指定：仅一种可用则自动选；两种都可用时默认金币
        if (canCoins) {
            return AssetLedgerService.COINS;
        }
        return AssetLedgerService.POINTS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IslandPetVo saveResidents(Long userId, IslandResidentSaveBo bo) {
        islandPetService.initForUser(userId);
        IslandPet pet = iIslandPetService.lambdaQuery().eq(IslandPet::getUserId, userId).one();
        if (pet == null) {
            throw new BizException(PetError.ISLAND_PET_NOT_FOUND);
        }
        List<Long> petIds = bo.getPetIds() == null ? Collections.emptyList() : bo.getPetIds();
        if (petIds.size() > 1) {
            throw new BizException("最多选择1只常驻宠物");
        }
        for (Long petId : petIds) {
            if (!userPetService.ownsPet(userId, petId)) {
                throw new BizException("只能设置自己领养的宠物为常驻");
            }
            PetArchive archive = iPetArchiveService.getById(petId);
            if (archive == null) {
                throw new BizException(PetError.INFO_NOT_FOUND);
            }
        }
        if (petIds.isEmpty()) {
            pet.setResidentPetId(null);
            pet.setResidentPetIds(null);
        } else {
            pet.setResidentPetId(petIds.get(0));
            pet.setResidentPetIds(String.valueOf(petIds.get(0)));
        }
        // 显式 set，避免 update-strategy=not_empty 时 null 无法清空旧常驻
        iIslandPetService.lambdaUpdate()
                .eq(IslandPet::getId, pet.getId())
                .set(IslandPet::getResidentPetId, pet.getResidentPetId())
                .set(IslandPet::getResidentPetIds, pet.getResidentPetIds())
                .update();
        // 回读最新数据，保证返回与库内一致
        IslandPet latest = iIslandPetService.getById(pet.getId());
        if (!petIds.isEmpty()) {
            try {
                islandTaskService.autoCompleteByAction(userId, "resident");
            } catch (Exception ignore) {
            }
        }
        IslandPetVo vo = new IslandPetVo();
        BeanUtils.copyProperties(latest != null ? latest : pet, vo);
        enrichVo(latest != null ? latest : pet, vo, userId);
        return vo;
    }

    @Override
    public List<PetArchiveVo> listResidentCandidates(Long userId) {
        List<PetArchiveVo> pets = userPetService.listMyPets(userId);
        // 若尚无常驻，默认第一只
        if (!pets.isEmpty()) {
            IslandPet island = iIslandPetService.lambdaQuery().eq(IslandPet::getUserId, userId).one();
            if (island != null && island.getResidentPetId() == null) {
                ensureDefaultResidentIfNeeded(userId, island);
            }
        }
        return pets;
    }

    private void ensureDefaultResidentIfNeeded(Long userId, IslandPet pet) {
        if (pet == null || pet.getResidentPetId() != null) {
            return;
        }
        Long firstPetId = userPetService.getFirstPetId(userId);
        if (firstPetId == null) {
            return;
        }
        iIslandPetService.lambdaUpdate()
                .eq(IslandPet::getId, pet.getId())
                .set(IslandPet::getResidentPetId, firstPetId)
                .set(IslandPet::getResidentPetIds, String.valueOf(firstPetId))
                .update();
        pet.setResidentPetId(firstPetId);
        pet.setResidentPetIds(String.valueOf(firstPetId));
    }

    private int resolveLoveGain(String feedType) {
        if ("water".equals(feedType) || "food".equals(feedType)) {
            return 1;
        }
        return 2;
    }

    /** 冷却结束后清空双碗，允许下一轮分别再喂 */
    private boolean refreshBowlCooldownState(IslandPet pet) {
        if (pet == null) {
            return false;
        }
        if (pet.getFeedCooldownUntil() != null && !pet.getFeedCooldownUntil().isAfter(LocalDateTime.now())) {
            pet.setFoodBowlFilled(0);
            pet.setWaterBowlFilled(0);
            pet.setFeedCooldownUntil(null);
            // update-strategy=not_empty 时 0/null 不会写入，必须显式 set
            iIslandPetService.lambdaUpdate()
                    .eq(IslandPet::getId, pet.getId())
                    .set(IslandPet::getFoodBowlFilled, 0)
                    .set(IslandPet::getWaterBowlFilled, 0)
                    .set(IslandPet::getFeedCooldownUntil, null)
                    .update();
            return true;
        }
        return false;
    }

    /** 持久化岛宠；显式写入可能为 0 的字段，规避 not_empty 策略 */
    private void saveIslandPet(IslandPet pet) {
        if (pet == null || pet.getId() == null) {
            return;
        }
        iIslandPetService.lambdaUpdate()
                .eq(IslandPet::getId, pet.getId())
                .set(IslandPet::getHunger, pet.getHunger())
                .set(IslandPet::getClean, pet.getClean())
                .set(IslandPet::getMood, pet.getMood())
                .set(IslandPet::getVitality, pet.getVitality())
                .set(IslandPet::getIntimacy, pet.getIntimacy())
                .set(IslandPet::getDailyProgress, pet.getDailyProgress())
                .set(IslandPet::getStatDate, pet.getStatDate())
                .set(IslandPet::getFoodBowlFilled, pet.getFoodBowlFilled() == null ? 0 : pet.getFoodBowlFilled())
                .set(IslandPet::getWaterBowlFilled, pet.getWaterBowlFilled() == null ? 0 : pet.getWaterBowlFilled())
                .set(IslandPet::getFeedCooldownUntil, pet.getFeedCooldownUntil())
                .update();
    }

    private boolean isCooling(IslandPet pet) {
        return pet.getFeedCooldownUntil() != null && pet.getFeedCooldownUntil().isAfter(LocalDateTime.now());
    }

    private boolean isBowlFilled(Integer flag) {
        return flag != null && flag == 1;
    }

    private void enrichVo(IslandPet pet, IslandPetVo vo, Long userId) {
        UserAssetVo asset = userAssetService.infoByUserId(userId);
        if (asset != null) {
            vo.setLoveValue(asset.getLoveValue());
            vo.setCoins(asset.getCoins());
            vo.setPoints(asset.getPoints());
            vo.setFlowers(asset.getFlowers());
            vo.setLevel(asset.getLevel());
            vo.setLevelName(asset.getLevelName());
        }
        int intimacy = pet.getIntimacy() == null ? 0 : pet.getIntimacy();
        vo.setIntimacyLevel(1 + intimacy / 100);
        // 顶部进度条改为「今日进度」；亲密度等级仍按 intimacy 计算
        int dailyProgress = pet.getDailyProgress() == null ? islandCareService.dailyStatInit() : pet.getDailyProgress();
        vo.setDailyProgress(Math.max(0, Math.min(100, dailyProgress)));
        vo.setIntimacyPercent(vo.getDailyProgress());
        vo.setStatusText(resolveStatusText(pet));
        vo.setMoodEmoji(resolveMoodEmoji(pet));
        boolean cooling = isCooling(pet);
        if (cooling) {
            long sec = ChronoUnit.SECONDS.between(LocalDateTime.now(), pet.getFeedCooldownUntil());
            int cooldown = (int) Math.max(0, sec);
            vo.setFeedCooldownSeconds(cooldown);
            vo.setFeedCooldownText(formatCooldown(cooldown));
        } else {
            vo.setFeedCooldownSeconds(0);
            vo.setFeedCooldownText("0:00");
        }
        boolean foodFilled = isBowlFilled(pet.getFoodBowlFilled());
        boolean waterFilled = isBowlFilled(pet.getWaterBowlFilled());
        vo.setFoodBowlFilled(foodFilled);
        vo.setWaterBowlFilled(waterFilled);
        vo.setCanFeedFood(!cooling && !foodFilled);
        vo.setCanFeedWater(!cooling && !waterFilled);
        vo.setCanFeed(Boolean.TRUE.equals(vo.getCanFeedFood()) || Boolean.TRUE.equals(vo.getCanFeedWater()));
        List<Long> residentIds = parseResidentIds(pet);
        vo.setResidents(buildResidents(residentIds));
        Long mainPetId = residentIds.isEmpty() ? pet.getResidentPetId() : residentIds.get(0);
        if (mainPetId != null) {
            PetArchive archive = iPetArchiveService.getById(mainPetId);
            if (archive != null && Objects.equals(archive.getAuditStatus(), 1)) {
                vo.setPetName(archive.getName());
                // 岛宅展示统一用品种插画，避免远程头像加载失败导致空白
                vo.setPetAvatar(resolveBreedAvatar(archive.getBreed(), archive.getPetType()));
                vo.setPetType(archive.getPetType());
                vo.setBreed(archive.getBreed());
                vo.setGender(archive.getGender());
                vo.setResidentPetId(mainPetId);
            }
        }
        if (StringUtils.isBlank(vo.getPetName())) {
            vo.setPetName("岛宠");
            vo.setPetAvatar(resolveBreedAvatar(vo.getBreed(), vo.getPetType()));
            vo.setMoodEmoji("🥰");
            vo.setStatusText("等待你的投喂");
        } else if (StringUtils.isBlank(vo.getPetAvatar()) || "/static/logo.png".equals(vo.getPetAvatar())) {
            vo.setPetAvatar(resolveBreedAvatar(vo.getBreed(), vo.getPetType()));
        }
        vo.setWeatherText("室内 24℃ · 湿度 55%");
        vo.setLeftActions(buildLeftActions(vo));
        vo.setRightActions(buildRightActions());
    }

    private List<IslandActionVo> buildLeftActions(IslandPetVo vo) {
        List<IslandActionVo> list = new ArrayList<>();
        // 侧栏「投喂」跳转投喂福利页（饭/水碗才走本地喂食）
        list.add(action("投喂", "🍗", "/packageA/feed/index", "navigate", true));
        list.add(action("家养", "🏠", "/packageA/home/setup", "navigate", true));
        list.add(action("任务", "📋", "/packageA/tasks/center", "navigate", true));
        list.add(action("集市", "🛍️", "/packageA/market/list", "navigate", true));
        return list;
    }

    private List<IslandActionVo> buildRightActions() {
        List<IslandActionVo> list = new ArrayList<>();
        list.add(action("发现新朋友", "🤝", "/packageA/post/create", "navigate", true));
        list.add(action("退休领养", "🏆", "/packageA/adopt/list", "navigate", true));
        list.add(action("人气打榜", "🎖️", "/packageA/ranking/list", "navigate", true));
        list.add(action("添加企微", "💼", "/packageA/wecom/index", "navigate", true));
        list.add(action("攻略", "📖", "/packageA/strategy/list", "navigate", true));
        return list;
    }

    private IslandActionVo action(String title, String emoji, String path, String actionType, Boolean enabled) {
        IslandActionVo vo = new IslandActionVo();
        vo.setTitle(title);
        vo.setEmoji(emoji);
        vo.setPath(path);
        vo.setActionType(actionType);
        vo.setEnabled(enabled == null || enabled);
        return vo;
    }

    private List<Long> parseResidentIds(IslandPet pet) {
        if (StringUtils.isNotBlank(pet.getResidentPetIds())) {
            return Arrays.stream(pet.getResidentPetIds().split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(Long::valueOf)
                    .limit(1)
                    .collect(Collectors.toList());
        }
        if (pet.getResidentPetId() != null) {
            return Collections.singletonList(pet.getResidentPetId());
        }
        return Collections.emptyList();
    }

    private List<IslandResidentVo> buildResidents(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<IslandResidentVo> list = new ArrayList<>();
        for (Long id : ids) {
            PetArchive archive = iPetArchiveService.getById(id);
            if (archive == null || !Objects.equals(archive.getAuditStatus(), 1)) {
                continue;
            }
            IslandResidentVo vo = new IslandResidentVo();
            vo.setId(archive.getId());
            vo.setName(archive.getName());
            vo.setAvatarUrl(StringUtils.defaultIfBlank(archive.getAvatarUrl(), "/static/logo.png"));
            vo.setPetType(archive.getPetType());
            vo.setBreed(archive.getBreed());
            if (archive.getStoreId() != null) {
                PetStore store = iPetStoreService.getById(archive.getStoreId());
                if (store != null) {
                    vo.setStoreName(store.getName());
                }
            }
            list.add(vo);
        }
        return list;
    }

    private String resolveStatusText(IslandPet pet) {
        int hunger = pet.getHunger() == null ? 80 : pet.getHunger();
        int mood = pet.getMood() == null ? 80 : pet.getMood();
        int clean = pet.getClean() == null ? 80 : pet.getClean();
        if (hunger < 35) {
            return "有点饿了";
        }
        if (clean < 40) {
            return "想洗个澡";
        }
        if (mood >= 75 && hunger >= 60) {
            return "正在吃东西";
        }
        if (mood >= 60) {
            return "心情不错";
        }
        return "需要你的陪伴";
    }

    private String resolveMoodEmoji(IslandPet pet) {
        int mood = pet.getMood() == null ? 80 : pet.getMood();
        if (mood >= 85) {
            return "🥰";
        }
        if (mood >= 65) {
            return "😊";
        }
        if (mood >= 45) {
            return "😐";
        }
        return "😿";
    }

    private String resolveIdempotentKey() {
        try {
            HttpServletRequest request = HttpContextUtil.getHttpServletRequest();
            return request.getHeader("Idempotent-Key");
        } catch (Exception e) {
            return null;
        }
    }

    private String formatCooldown(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return min + ":" + String.format("%02d", sec);
    }

    /** 按品种/类型给出岛宅默认展示图（与前端 static/pets 资源对应） */
    private String resolveBreedAvatar(String breed, String petType) {
        String b = breed == null ? "" : breed.trim();
        if (b.contains("柴") || b.toLowerCase().contains("shiba")) {
            return "/static/pets/island-pet-shiba.png";
        }
        if (b.contains("金毛") || b.toLowerCase().contains("golden")) {
            return "/static/pets/island-pet-golden.png";
        }
        if (b.contains("柯基") || b.toLowerCase().contains("corgi")) {
            return "/static/pets/island-pet-corgi.png";
        }
        if (b.contains("法斗") || b.contains("法牛") || b.contains("法国斗牛") || b.toLowerCase().contains("french")) {
            return "/static/pets/island-pet-frenchie.png";
        }
        if (b.contains("萨摩") || b.toLowerCase().contains("samoyed")) {
            return "/static/pets/island-pet-samoyed.png";
        }
        if (b.contains("三花") || b.contains("狸花") || b.contains("橘") || b.contains("加菲")
                || b.contains("美短") || b.contains("英短") || b.contains("蓝猫") || b.contains("暹罗") || b.contains("波斯")) {
            return "/static/pets/island-pet-calico.png";
        }
        if (b.contains("白猫") || "白".equals(b)) {
            return "/static/pets/island-pet-white-cat.png";
        }
        if (b.contains("布偶") || b.toLowerCase().contains("ragdoll")) {
            return "/static/pets/island-pet-ragdoll.png";
        }
        if (b.contains("泰迪") || b.contains("贵宾") || b.contains("比熊") || b.contains("博美")) {
            return "/static/pets/island-pet-shiba.png";
        }
        String t = petType == null ? "" : petType.trim().toLowerCase();
        if (t.contains("cat") || t.contains("猫") || "2".equals(t)) {
            return "/static/pets/island-pet-calico.png";
        }
        return "/static/pets/island-pet-shiba.png";
    }
}
