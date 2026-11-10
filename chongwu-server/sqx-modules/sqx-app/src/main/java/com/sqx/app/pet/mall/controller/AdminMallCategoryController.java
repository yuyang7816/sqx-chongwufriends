package com.sqx.app.pet.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.mall.db.service.IMallCategoryService;
import com.sqx.app.pet.mall.db.service.IMallSpuService;
import com.sqx.app.pet.mall.domain.entity.MallCategory;
import com.sqx.app.pet.mall.domain.entity.MallSpu;
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

@Tag(name = "Admin-Mall Category", description = "商城分类管理")
@RestController
@RequestMapping("/admin/pet/mall/category")
public class AdminMallCategoryController {

    @Resource
    private IMallCategoryService iMallCategoryService;
    @Resource
    private IMallSpuService iMallSpuService;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CategoryPageBo extends PageParam {
        private String name;
        private Integer status;
    }

    @Operation(summary = "分类分页")
    @PostMapping("page")
    public Result<IPage<MallCategory>> page(@RequestBody CategoryPageBo bo) {
        Page<MallCategory> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<MallCategory> qw = new QueryWrapper<>();
        qw.lambda()
                .like(StringUtils.isNotBlank(bo.getName()), MallCategory::getName, bo.getName())
                .eq(bo.getStatus() != null, MallCategory::getStatus, bo.getStatus())
                .orderByAsc(MallCategory::getSort)
                .orderByDesc(MallCategory::getCreateTime);
        return Result.success(iMallCategoryService.page(page, qw));
    }

    @Operation(summary = "分类列表")
    @PostMapping("list")
    public Result<List<MallCategory>> list() {
        QueryWrapper<MallCategory> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(MallCategory::getStatus, 1)
                .orderByAsc(MallCategory::getSort);
        return Result.success(iMallCategoryService.list(qw));
    }

    @Operation(summary = "保存分类")
    @PostMapping("save")
    public Result<Void> save(@RequestBody MallCategory category) {
        if (StringUtils.isBlank(category.getName())) {
            throw new BizException("分类名称不能为空");
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        iMallCategoryService.save(category);
        return Result.success();
    }

    @Operation(summary = "更新分类")
    @PostMapping("update")
    public Result<Void> update(@RequestBody MallCategory category) {
        if (category.getId() == null) {
            throw new BizException("分类ID不能为空");
        }
        if (StringUtils.isBlank(category.getName())) {
            throw new BizException("分类名称不能为空");
        }
        iMallCategoryService.updateById(category);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody MallCategory category) {
        if (category.getId() == null) {
            throw new BizException("分类ID不能为空");
        }
        long count = iMallSpuService.lambdaQuery()
                .eq(MallSpu::getCategoryId, category.getId())
                .count();
        if (count > 0) {
            throw new BizException("该分类下仍有商品，无法删除");
        }
        iMallCategoryService.removeById(category.getId());
        return Result.success();
    }
}
