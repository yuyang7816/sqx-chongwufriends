package com.sqx.app.pet.island.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.app.pet.island.db.service.IIslandMarketItemService;
import com.sqx.app.pet.island.domain.entity.IslandMarketItem;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.domain.PageParam;
import com.sqx.common.mybatis.utils.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "管理端-岛宅集市")
@RestController
@RequestMapping("/admin/pet/island/market")
public class AdminIslandMarketController {

    @Resource
    private IIslandMarketItemService iIslandMarketItemService;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class MarketPageBo extends PageParam {
        private String petType;
    }

    @Data
    public static class MarketSaveBo {
        private Long id;
        private String name;
        private String coverUrl;
        private String itemType;
        private Integer priceCoins;
        private Integer pricePoints;
        private String effectDesc;
        private Integer durability;
        private Integer favor;
        private String petType;
        private Integer sort;
        private Integer status;
    }

    @Data
    public static class IdBo {
        private Long id;
    }

    @Operation(summary = "分页")
    @PostMapping("page")
    public Result<IPage<IslandMarketItem>> page(@RequestBody @Validated MarketPageBo bo) {
        Page<IslandMarketItem> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<IslandMarketItem> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(bo.getPetType() != null && !bo.getPetType().isEmpty(), IslandMarketItem::getPetType, bo.getPetType())
                .orderByAsc(IslandMarketItem::getSort);
        return Result.success(iIslandMarketItemService.page(page, qw));
    }

    @Operation(summary = "详情")
    @PostMapping("info")
    public Result<IslandMarketItem> info(@RequestBody IdBo bo) {
        IslandMarketItem item = iIslandMarketItemService.getById(bo.getId());
        if (item == null) {
            throw new BizException("道具不存在");
        }
        return Result.success(item);
    }

    @Idempotent
    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestBody MarketSaveBo bo) {
        IslandMarketItem entity = new IslandMarketItem();
        entity.setName(bo.getName());
        entity.setCoverUrl(bo.getCoverUrl());
        entity.setItemType(bo.getItemType());
        entity.setPriceCoins(bo.getPriceCoins());
        entity.setPricePoints(bo.getPricePoints());
        entity.setEffectDesc(bo.getEffectDesc());
        entity.setDurability(bo.getDurability());
        entity.setFavor(bo.getFavor());
        entity.setPetType(bo.getPetType());
        entity.setSort(bo.getSort());
        entity.setStatus(bo.getStatus() == null ? 1 : bo.getStatus());
        iIslandMarketItemService.save(entity);
        return Result.success();
    }

    @Idempotent
    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody MarketSaveBo bo) {
        IslandMarketItem entity = iIslandMarketItemService.getById(bo.getId());
        if (entity == null) {
            throw new BizException("道具不存在");
        }
        entity.setName(bo.getName());
        entity.setCoverUrl(bo.getCoverUrl());
        entity.setItemType(bo.getItemType());
        entity.setPriceCoins(bo.getPriceCoins());
        entity.setPricePoints(bo.getPricePoints());
        entity.setEffectDesc(bo.getEffectDesc());
        entity.setDurability(bo.getDurability());
        entity.setFavor(bo.getFavor());
        entity.setPetType(bo.getPetType());
        entity.setSort(bo.getSort());
        if (bo.getStatus() != null) {
            entity.setStatus(bo.getStatus());
        }
        iIslandMarketItemService.updateById(entity);
        return Result.success();
    }

    @Idempotent
    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody IdBo bo) {
        iIslandMarketItemService.removeById(bo.getId());
        return Result.success();
    }
}
