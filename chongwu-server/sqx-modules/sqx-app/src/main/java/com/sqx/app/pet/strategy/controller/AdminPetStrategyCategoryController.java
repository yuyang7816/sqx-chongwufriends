package com.sqx.app.pet.strategy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.strategy.db.service.IPetStrategyCategoryService;
import com.sqx.app.pet.strategy.db.service.IPetStrategyService;
import com.sqx.app.pet.strategy.domain.entity.PetStrategy;
import com.sqx.app.pet.strategy.domain.entity.PetStrategyCategory;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.domain.PageParam;
import com.sqx.common.mybatis.utils.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "Admin-Pet Strategy Category", description = "养宠攻略分类管理")
@RestController
@RequestMapping("/admin/pet/strategy/category")
public class AdminPetStrategyCategoryController {

    @Resource
    private IPetStrategyCategoryService iPetStrategyCategoryService;
    @Resource
    private IPetStrategyService iPetStrategyService;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CategoryPageBo extends PageParam {
        private String name;
        private Integer status;
    }

    @Operation(summary = "分类分页")
    @PostMapping("page")
    public Result<IPage<PetStrategyCategory>> page(@RequestBody CategoryPageBo bo) {
        Page<PetStrategyCategory> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<PetStrategyCategory> qw = new QueryWrapper<>();
        qw.lambda()
                .like(StringUtils.isNotBlank(bo.getName()), PetStrategyCategory::getName, bo.getName())
                .eq(bo.getStatus() != null, PetStrategyCategory::getStatus, bo.getStatus())
                .orderByAsc(PetStrategyCategory::getSort)
                .orderByAsc(PetStrategyCategory::getId);
        return Result.success(iPetStrategyCategoryService.page(page, qw));
    }

    @Operation(summary = "分类列表（启用）")
    @PostMapping("list")
    public Result<List<PetStrategyCategory>> list() {
        QueryWrapper<PetStrategyCategory> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(PetStrategyCategory::getStatus, 1)
                .orderByAsc(PetStrategyCategory::getSort)
                .orderByAsc(PetStrategyCategory::getId);
        return Result.success(iPetStrategyCategoryService.list(qw));
    }

    @Operation(summary = "保存分类")
    @PostMapping("save")
    public Result<Void> save(@RequestBody PetStrategyCategory category) {
        if (StringUtils.isBlank(category.getName())) {
            throw new BizException("分类名称不能为空");
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        iPetStrategyCategoryService.save(category);
        return Result.success();
    }

    @Operation(summary = "更新分类")
    @PostMapping("update")
    public Result<Void> update(@RequestBody PetStrategyCategory category) {
        if (category.getId() == null) {
            throw new BizException("分类ID不能为空");
        }
        if (StringUtils.isBlank(category.getName())) {
            throw new BizException("分类名称不能为空");
        }
        PetStrategyCategory existing = iPetStrategyCategoryService.getById(category.getId());
        if (existing == null) {
            throw new BizException("分类不存在");
        }
        iPetStrategyCategoryService.updateById(category);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody PetStrategyCategory category) {
        if (category.getId() == null) {
            throw new BizException("分类ID不能为空");
        }
        long count = iPetStrategyService.lambdaQuery()
                .eq(PetStrategy::getCategoryId, category.getId())
                .count();
        if (count > 0) {
            throw new BizException("该分类下仍有攻略，无法删除");
        }
        iPetStrategyCategoryService.removeById(category.getId());
        return Result.success();
    }
}
