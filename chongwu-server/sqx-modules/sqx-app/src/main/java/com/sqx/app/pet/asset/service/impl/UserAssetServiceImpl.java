package com.sqx.app.pet.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.support.UserIdResolveHelper;
import com.sqx.app.pet.asset.db.service.IUserAssetService;
import com.sqx.app.pet.asset.domain.bo.*;
import com.sqx.app.pet.asset.domain.entity.UserAsset;
import com.sqx.app.pet.asset.domain.vo.UserAssetVo;
import com.sqx.app.pet.asset.service.UserAssetService;
import com.sqx.app.pet.common.service.AssetLedgerService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserAssetServiceImpl implements UserAssetService {

    @Resource
    private IUserAssetService iUserAssetService;
    @Resource
    private UserIdResolveHelper userIdResolveHelper;
    @Resource
    private AssetLedgerService assetLedgerService;

    @Override
    public void saveInfo(UserAssetSaveBo bo) {
        UserAsset entity = new UserAsset();
        BeanUtils.copyProperties(bo, entity);
        iUserAssetService.save(entity);
    }

    @Override
    public IPage<UserAssetVo> pageInfo(UserAssetPageBo bo) {
        Page<UserAsset> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        List<Long> userIds = userIdResolveHelper.resolveOptional(bo.getUserId(), bo.getPhone(), bo.getUsername());
        if (userIds != null && userIds.isEmpty()) {
            return PageUtil.copyPage(page, Collections.emptyList());
        }
        QueryWrapper<UserAsset> qw = new QueryWrapper<>();
        qw.lambda()
                .in(userIds != null, UserAsset::getUserId, userIds)
                .orderByDesc(UserAsset::getCreateTime);
        IPage<UserAsset> iPage = iUserAssetService.page(page, qw);
        List<UserAssetVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (UserAsset record : iPage.getRecords()) {
                UserAssetVo vo = new UserAssetVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public UserAssetVo info(UserAssetInfoBo bo) {
        UserAsset entity = iUserAssetService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException("not found");
        }
        UserAssetVo vo = new UserAssetVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void updateInfo(UserAssetUpdateBo bo) {
        UserAsset entity = new UserAsset();
        BeanUtils.copyProperties(bo, entity);
        iUserAssetService.updateById(entity);
    }

    @Override
    public void delete(UserAssetInfoBo bo) {
        iUserAssetService.removeById(bo.getId());
    }

    @Override
    public List<UserAssetVo> list() {
        List<UserAsset> list = iUserAssetService.list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            UserAssetVo vo = new UserAssetVo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void initForUser(Long userId) {
        QueryWrapper<UserAsset> qw = new QueryWrapper<>();
        qw.lambda().eq(UserAsset::getUserId, userId);
        if (iUserAssetService.count(qw) > 0) {
            return;
        }
        UserAsset asset = new UserAsset();
        asset.setUserId(userId);
        asset.setPoints(0);
        asset.setCoins(0);
        asset.setLoveValue(0);
        asset.setFlowers(0);
        asset.setLevel(1);
        asset.setLevelName("Lv.1");
        iUserAssetService.save(asset);
    }

    @Override
    public UserAssetVo infoByUserId(Long userId) {
        QueryWrapper<UserAsset> qw = new QueryWrapper<>();
        qw.lambda().eq(UserAsset::getUserId, userId).last("limit 1");
        UserAsset entity = iUserAssetService.getOne(qw);
        if (Objects.isNull(entity)) {
            initForUser(userId);
            entity = iUserAssetService.getOne(qw);
        }
        UserAssetVo vo = new UserAssetVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void adjust(UserAssetAdjustBo bo) {
        Long userId = bo.getUserId();
        String type = bo.getAssetType();
        int amount = bo.getAmount() == null ? 0 : bo.getAmount();
        String op = bo.getOpType() == null ? "add" : bo.getOpType().trim().toLowerCase();
        String reason = bo.getReason() == null || bo.getReason().trim().isEmpty() ? "管理员调整" : bo.getReason().trim();
        if (amount < 0) {
            throw new BizException("数量不能为负数");
        }
        switch (op) {
            case "add":
                if (amount == 0) return;
                assetLedgerService.change(userId, type, amount, 1, "admin_adjust", null, reason);
                break;
            case "reduce":
                if (amount == 0) return;
                assetLedgerService.change(userId, type, amount, 2, "admin_adjust", null, reason);
                break;
            case "set":
                UserAsset asset = iUserAssetService.lambdaQuery().eq(UserAsset::getUserId, userId).one();
                if (Objects.isNull(asset)) {
                    initForUser(userId);
                    asset = iUserAssetService.lambdaQuery().eq(UserAsset::getUserId, userId).one();
                }
                int current = currentValue(asset, type);
                int diff = amount - current;
                if (diff == 0) return;
                assetLedgerService.change(userId, type, Math.abs(diff), diff > 0 ? 1 : 2, "admin_adjust", null, reason);
                break;
            default:
                throw new BizException("不支持的操作类型：" + op);
        }
    }

    private int currentValue(UserAsset asset, String type) {
        switch (type) {
            case AssetLedgerService.POINTS: return asset.getPoints() == null ? 0 : asset.getPoints();
            case AssetLedgerService.COINS: return asset.getCoins() == null ? 0 : asset.getCoins();
            case AssetLedgerService.LOVE: return asset.getLoveValue() == null ? 0 : asset.getLoveValue();
            case AssetLedgerService.FLOWERS: return asset.getFlowers() == null ? 0 : asset.getFlowers();
            default: throw new BizException("资产类型无效：" + type);
        }
    }
}
