package com.sqx.app.pet.island.service.impl;

import com.sqx.app.pet.island.db.service.IIslandPetService;
import com.sqx.app.pet.island.domain.entity.IslandPet;
import com.sqx.app.pet.island.service.IslandCareService;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;

@Service
public class IslandCareServiceImpl implements IslandCareService {

    public static final String CFG_DAILY_STAT_INIT = "island_daily_stat_init";
    public static final String CFG_FEED_PROGRESS = "island_feed_progress_gain";
    public static final String CFG_RANKING_PROGRESS = "island_ranking_progress_gain";
    public static final String CFG_GIFT_PROGRESS = "island_gift_progress_gain";
    public static final String CFG_FEED_HUNGER = "island_feed_hunger_gain";
    public static final String CFG_FEED_MOOD = "island_feed_mood_gain";
    public static final String CFG_RANKING_CLEAN = "island_ranking_clean_gain";
    public static final String CFG_RANKING_VITALITY = "island_ranking_vitality_gain";
    public static final String CFG_MARKET_CLEAN = "island_market_clean_gain";
    public static final String CFG_MARKET_VITALITY = "island_market_vitality_gain";
    public static final String CFG_GIFT_CLEAN = "island_gift_clean_gain";
    public static final String CFG_GIFT_VITALITY = "island_gift_vitality_gain";
    public static final String CFG_FEED_COOLDOWN = "island_feed_cooldown_minutes";

    private static final int DEFAULT_INIT = 50;
    private static final int DEFAULT_FEED_PROGRESS = 6;
    private static final int DEFAULT_RANKING_PROGRESS = 2;
    private static final int DEFAULT_GIFT_PROGRESS = 4;
    private static final int DEFAULT_FEED_HUNGER = 12;
    private static final int DEFAULT_FEED_MOOD = 10;
    private static final int DEFAULT_RANKING_CLEAN = 6;
    private static final int DEFAULT_RANKING_VITALITY = 6;
    private static final int DEFAULT_MARKET_CLEAN = 8;
    private static final int DEFAULT_MARKET_VITALITY = 8;
    private static final int DEFAULT_GIFT_CLEAN = 8;
    private static final int DEFAULT_GIFT_VITALITY = 8;
    private static final int DEFAULT_COOLDOWN_MINUTES = 30;

    @Resource
    private IIslandPetService iIslandPetService;

    @Override
    public void ensureDailyReset(IslandPet pet) {
        if (pet == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (pet.getStatDate() != null && today.equals(pet.getStatDate())) {
            return;
        }
        int init = dailyStatInit();
        pet.setHunger(init);
        pet.setClean(init);
        pet.setMood(init);
        pet.setVitality(init);
        pet.setDailyProgress(init);
        pet.setStatDate(today);
    }

    @Override
    public void applyFeedCare(IslandPet pet) {
        if (pet == null) {
            return;
        }
        ensureDailyReset(pet);
        addProgress(pet, configInt(CFG_FEED_PROGRESS, DEFAULT_FEED_PROGRESS));
        pet.setHunger(cap100(nvl(pet.getHunger()) + configInt(CFG_FEED_HUNGER, DEFAULT_FEED_HUNGER)));
        pet.setMood(cap100(nvl(pet.getMood()) + configInt(CFG_FEED_MOOD, DEFAULT_FEED_MOOD)));
        // 亲密度仍用于等级，喂一次小幅增加
        pet.setIntimacy(nvl(pet.getIntimacy()) + 2);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRankingCare(Long userId) {
        if (userId == null) {
            return;
        }
        IslandPet pet = iIslandPetService.lambdaQuery().eq(IslandPet::getUserId, userId).one();
        if (pet == null) {
            return;
        }
        ensureDailyReset(pet);
        addProgress(pet, configInt(CFG_RANKING_PROGRESS, DEFAULT_RANKING_PROGRESS));
        pet.setClean(cap100(nvl(pet.getClean()) + configInt(CFG_RANKING_CLEAN, DEFAULT_RANKING_CLEAN)));
        pet.setVitality(cap100(nvl(pet.getVitality()) + configInt(CFG_RANKING_VITALITY, DEFAULT_RANKING_VITALITY)));
        saveCareFields(pet);
    }

    @Override
    public void applyMarketCare(IslandPet pet) {
        if (pet == null) {
            return;
        }
        ensureDailyReset(pet);
        addProgress(pet, configInt(CFG_GIFT_PROGRESS, DEFAULT_GIFT_PROGRESS));
        pet.setClean(cap100(nvl(pet.getClean()) + configInt(CFG_MARKET_CLEAN, DEFAULT_MARKET_CLEAN)));
        pet.setVitality(cap100(nvl(pet.getVitality()) + configInt(CFG_MARKET_VITALITY, DEFAULT_MARKET_VITALITY)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyGiftCare(Long userId) {
        if (userId == null) {
            return;
        }
        IslandPet pet = iIslandPetService.lambdaQuery().eq(IslandPet::getUserId, userId).one();
        if (pet == null) {
            return;
        }
        ensureDailyReset(pet);
        addProgress(pet, configInt(CFG_GIFT_PROGRESS, DEFAULT_GIFT_PROGRESS));
        pet.setClean(cap100(nvl(pet.getClean()) + configInt(CFG_GIFT_CLEAN, DEFAULT_GIFT_CLEAN)));
        pet.setVitality(cap100(nvl(pet.getVitality()) + configInt(CFG_GIFT_VITALITY, DEFAULT_GIFT_VITALITY)));
        saveCareFields(pet);
    }

    @Override
    public int feedCooldownMinutes() {
        return Math.max(1, configInt(CFG_FEED_COOLDOWN, DEFAULT_COOLDOWN_MINUTES));
    }

    @Override
    public int dailyStatInit() {
        return Math.max(0, Math.min(100, configInt(CFG_DAILY_STAT_INIT, DEFAULT_INIT)));
    }

    private void saveCareFields(IslandPet pet) {
        iIslandPetService.lambdaUpdate()
                .eq(IslandPet::getId, pet.getId())
                .set(IslandPet::getHunger, pet.getHunger())
                .set(IslandPet::getClean, pet.getClean())
                .set(IslandPet::getMood, pet.getMood())
                .set(IslandPet::getVitality, pet.getVitality())
                .set(IslandPet::getDailyProgress, pet.getDailyProgress())
                .set(IslandPet::getStatDate, pet.getStatDate())
                .set(IslandPet::getIntimacy, pet.getIntimacy())
                .update();
    }

    private void addProgress(IslandPet pet, int gain) {
        pet.setDailyProgress(cap100(nvl(pet.getDailyProgress()) + Math.max(0, gain)));
    }

    private int configInt(String key, int defaultVal) {
        try {
            String raw = ConfigUtil.getConfigValueOrDefault(key);
            if (StringUtils.isBlank(raw)) {
                return defaultVal;
            }
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private static int cap100(int v) {
        return Math.max(0, Math.min(100, v));
    }
}
