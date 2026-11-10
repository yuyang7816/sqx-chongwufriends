package com.sqx.app.pet.island.service;

import com.sqx.app.pet.island.domain.entity.IslandPet;

/**
 * 岛宅每日属性/进度与行为加成（喂食、打榜、集市、送东西）
 */
public interface IslandCareService {

    /** 跨日则重置饱食/清洁/心情/活力/今日进度为配置初始值 */
    void ensureDailyReset(IslandPet pet);

    /** 喂饭/喂水：进度 + 饱食度 + 心情 */
    void applyFeedCare(IslandPet pet);

    /** 打榜：进度 + 清洁度 + 活力值 */
    void applyRankingCare(Long userId);

    /** 集市兑换：进度 + 清洁度 + 活力值（另保留 favor→亲密度） */
    void applyMarketCare(IslandPet pet);

    /** 门店送东西（投喂订单支付成功）：进度 + 清洁度 + 活力值 */
    void applyGiftCare(Long userId);

    int feedCooldownMinutes();

    int dailyStatInit();
}
