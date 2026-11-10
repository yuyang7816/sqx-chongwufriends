package com.sqx.app.pet.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.mall.domain.bo.MallSkuSpecPageBo;
import com.sqx.app.pet.mall.domain.entity.MallSkuSpec;
import com.sqx.app.pet.mall.service.MallSkuSpecService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "Admin-Mall SKU Spec", description = "商城规格模板管理")
@RestController
@RequestMapping("/admin/pet/mall/sku-spec")
public class AdminMallSkuSpecController {

    @Resource
    private MallSkuSpecService mallSkuSpecService;

    @Operation(summary = "规格模板分页")
    @PostMapping("page")
    public Result<IPage<MallSkuSpec>> page(@RequestBody @Validated MallSkuSpecPageBo bo) {
        return Result.success(mallSkuSpecService.pageInfo(bo));
    }

    @Operation(summary = "启用规格列表")
    @PostMapping("list")
    public Result<List<MallSkuSpec>> list() {
        return Result.success(mallSkuSpecService.listEnabled());
    }

    @Operation(summary = "新增规格模板")
    @PostMapping("save")
    public Result<Void> save(@RequestBody MallSkuSpec spec) {
        mallSkuSpecService.saveInfo(spec);
        return Result.success();
    }

    @Operation(summary = "更新规格模板")
    @PostMapping("update")
    public Result<Void> update(@RequestBody MallSkuSpec spec) {
        mallSkuSpecService.updateInfo(spec);
        return Result.success();
    }

    @Operation(summary = "删除规格模板")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody MallSkuSpec spec) {
        if (spec.getId() == null) {
            throw new BizException("规格ID不能为空");
        }
        mallSkuSpecService.delete(spec.getId());
        return Result.success();
    }
}
