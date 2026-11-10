package com.sqx.app.pet.city.controller;

import com.sqx.app.pet.city.domain.bo.*;
import com.sqx.app.pet.city.domain.vo.PetCityVo;
import com.sqx.app.pet.city.service.PetCityService;
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
import java.util.List;

@Tag(name = "App-Pet City", description = "App-Pet City")
@RestController
@RequestMapping("/app/pet/city")
public class AppPetCityController {

    @Resource
    private PetCityService petCity;

    @Operation(summary = "list")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetCityVo.class)))
    @PostMapping("list")
    public Result<List<PetCityVo>> list() {
        return Result.success(petCity.list());
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetCityVo.class)))
    @PostMapping("page")
    public Result<IPage<PetCityVo>> page(@RequestBody @Validated PetCityPageBo bo) {
        return Result.success(petCity.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PetCityVo.class)))
    @PostMapping("info")
    public Result<PetCityVo> info(@RequestBody @Validated PetCityInfoBo bo) {
        return Result.success(petCity.info(bo));
    }
}
