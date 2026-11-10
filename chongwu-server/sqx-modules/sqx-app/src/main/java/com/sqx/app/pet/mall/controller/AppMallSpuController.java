package com.sqx.app.pet.mall.controller;

import com.sqx.app.pet.mall.db.service.IMallCategoryService;
import com.sqx.app.pet.mall.domain.bo.*;
import com.sqx.app.pet.mall.domain.entity.MallCategory;
import com.sqx.app.pet.mall.domain.vo.MallSpuVo;
import com.sqx.app.pet.mall.service.MallSpuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "App-Mall SPU", description = "App-Mall SPU")
@RestController
@RequestMapping("/app/pet/mall")
public class AppMallSpuController {

    @Resource
    private MallSpuService mallSpu;
    @Resource
    private IMallCategoryService iMallCategoryService;

    @Operation(summary = "分类列表")
    @PostMapping("categories")
    public Result<List<MallCategory>> categories() {
        QueryWrapper<MallCategory> qw = new QueryWrapper<>();
        qw.lambda().eq(MallCategory::getStatus, 1).orderByAsc(MallCategory::getSort);
        List<MallCategory> list = iMallCategoryService.list(qw);
        if (list == null || list.isEmpty()) {
            list = defaultCategories();
        }
        return Result.success(list);
    }

    private List<MallCategory> defaultCategories() {
        List<MallCategory> list = new ArrayList<>();
        list.add(category(1L, "全部商品", 0));
        list.add(category(2L, "客户投喂", 1));
        list.add(category(3L, "超值次卡", 2));
        list.add(category(4L, "实物赠予", 3));
        return list;
    }

    private MallCategory category(Long id, String name, int sort) {
        MallCategory c = new MallCategory();
        c.setId(id);
        c.setName(name);
        c.setSort(sort);
        c.setStatus(1);
        return c;
    }

    @Operation(summary = "list")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MallSpuVo.class)))
    @PostMapping("list")
    public Result<List<MallSpuVo>> list() {
        return Result.success(mallSpu.list());
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MallSpuVo.class)))
    @PostMapping("page")
    public Result<IPage<MallSpuVo>> page(@RequestBody @Validated MallSpuPageBo bo) {
        return Result.success(mallSpu.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MallSpuVo.class)))
    @PostMapping("info")
    public Result<MallSpuVo> info(@RequestBody @Validated MallSpuInfoBo bo) {
        return Result.success(mallSpu.info(bo));
    }
}
