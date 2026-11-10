package com.sqx.app.pet.breed.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.breed.domain.bo.*;
import com.sqx.app.pet.breed.domain.vo.PetBreedVo;
import com.sqx.app.pet.breed.service.PetBreedService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "Admin-Pet Breed", description = "品种字典管理")
@RestController
@RequestMapping("/admin/pet/breed")
public class AdminPetBreedController {

    @Resource
    private PetBreedService petBreedService;

    @Operation(summary = "page")
    @PostMapping("page")
    public Result<IPage<PetBreedVo>> page(@RequestBody @Validated PetBreedPageBo bo) {
        return Result.success(petBreedService.pageInfo(bo));
    }

    @Operation(summary = "list")
    @PostMapping("list")
    public Result<List<PetBreedVo>> list(@RequestBody(required = false) PetBreedListBo bo) {
        String petType = bo == null ? null : bo.getPetType();
        return Result.success(petBreedService.listEnabled(petType));
    }

    @Operation(summary = "save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated PetBreedSaveBo bo) {
        petBreedService.saveInfo(bo);
        return Result.success();
    }

    @Operation(summary = "update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated PetBreedUpdateBo bo) {
        petBreedService.updateInfo(bo);
        return Result.success();
    }

    @Operation(summary = "delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated PetBreedInfoBo bo) {
        petBreedService.delete(bo);
        return Result.success();
    }
}
