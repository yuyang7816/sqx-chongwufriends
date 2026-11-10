package com.sqx.app.pet.strategy.controller;

import com.sqx.app.pet.strategy.domain.bo.*;
import com.sqx.app.pet.strategy.domain.vo.PetStrategyCategoryVo;
import com.sqx.app.pet.strategy.domain.vo.PetStrategyVo;
import com.sqx.app.pet.strategy.service.PetStrategyService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.common.core.domain.Result;
import com.sqx.common.shiro.utils.UserContextHolder;
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
import java.util.List;

@Tag(name = "App-Pet Strategy", description = "App-Pet Strategy")
@RestController
@RequestMapping("/app/pet/strategy")
public class AppPetStrategyController {

    @Resource
    private PetStrategyService petStrategy;

    @Operation(summary = "分类列表")
    @PostMapping("categories")
    public Result<List<PetStrategyCategoryVo>> categories() {
        return Result.success(petStrategy.categories());
    }

    @Operation(summary = "list")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetStrategyVo.class)))
    @PostMapping("list")
    public Result<List<PetStrategyVo>> list() {
        return Result.success(petStrategy.list());
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetStrategyVo.class)))
    @PostMapping("page")
    public Result<IPage<PetStrategyVo>> page(@RequestBody @Validated PetStrategyPageBo bo) {
        // 用户端只展示已发布
        bo.setStatus(1);
        return Result.success(petStrategy.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetStrategyVo.class)))
    @PostMapping("info")
    public Result<PetStrategyVo> info(@RequestBody @Validated PetStrategyInfoBo bo) {
        Long userId = null;
        try {
            userId = UserContextHolder.getAppUserId();
        } catch (Exception ignored) {
            // 未登录也可阅读，登录用户才计阅读数
        }
        return Result.success(petStrategy.infoWithRead(userId, bo));
    }
}
