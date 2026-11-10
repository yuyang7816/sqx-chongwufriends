package com.sqx.app.pet.common.service;

import com.sqx.app.pet.asset.db.service.IUserAssetService;
import com.sqx.app.pet.asset.domain.entity.UserAsset;
import com.sqx.app.pet.common.domain.entity.UserAssetLedger;
import com.sqx.app.pet.common.db.service.IUserAssetLedgerService;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Slf4j
@Service
public class AssetLedgerService {

    public static final String POINTS = "points";
    public static final String COINS = "coins";
    public static final String LOVE = "love";
    public static final String FLOWERS = "flowers";

    @Resource
    private IUserAssetService iUserAssetService;
    @Resource
    private IUserAssetLedgerService iUserAssetLedgerService;

    @Transactional(rollbackFor = Exception.class)
    public void change(Long userId, String assetType, int amount, int direction, String bizType, Long bizId, String title) {
        UserAsset asset = getOrInit(userId);
        int before = getValue(asset, assetType);
        if (direction == 2 && before < amount) {
            throw new BizException(PetError.ASSET_NOT_ENOUGH);
        }
        int after = direction == 1 ? before + amount : before - amount;
        setValue(asset, assetType, after);
        iUserAssetService.updateById(asset);

        UserAssetLedger ledger = new UserAssetLedger();
        ledger.setUserId(userId);
        ledger.setAssetType(assetType);
        ledger.setChangeAmount(amount);
        ledger.setDirection(direction);
        ledger.setBizType(bizType);
        ledger.setBizId(bizId);
        ledger.setTitle(title);
        iUserAssetLedgerService.save(ledger);
        log.info("[资产流水] userId={} type={} dir={} amt={} biz={}:{}", userId, assetType, direction, amount, bizType, bizId);
    }

    private UserAsset getOrInit(Long userId) {
        UserAsset asset = iUserAssetService.lambdaQuery().eq(UserAsset::getUserId, userId).one();
        if (Objects.isNull(asset)) {
            asset = new UserAsset();
            asset.setUserId(userId);
            asset.setPoints(0);
            asset.setCoins(0);
            asset.setLoveValue(0);
            asset.setFlowers(0);
            asset.setLevel(1);
            asset.setLevelName("Lv.1");
            iUserAssetService.save(asset);
        }
        return asset;
    }

    private int getValue(UserAsset asset, String type) {
        switch (type) {
            case POINTS: return asset.getPoints() == null ? 0 : asset.getPoints();
            case COINS: return asset.getCoins() == null ? 0 : asset.getCoins();
            case LOVE: return asset.getLoveValue() == null ? 0 : asset.getLoveValue();
            case FLOWERS: return asset.getFlowers() == null ? 0 : asset.getFlowers();
            default: throw new BizException(PetError.ASSET_TYPE_INVALID);
        }
    }

    private void setValue(UserAsset asset, String type, int val) {
        switch (type) {
            case POINTS: asset.setPoints(val); break;
            case COINS: asset.setCoins(val); break;
            case LOVE: asset.setLoveValue(val); break;
            case FLOWERS: asset.setFlowers(val); break;
            default: throw new BizException(PetError.ASSET_TYPE_INVALID);
        }
    }
}
