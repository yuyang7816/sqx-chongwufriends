package com.sqx.app.pet.breed.controller;

import com.sqx.app.pet.breed.domain.bo.PetBreedListBo;
import com.sqx.app.pet.breed.domain.vo.PetBreedVo;
import com.sqx.app.pet.breed.service.PetBreedService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "App-Pet Breed", description = "品种字典")
@RestController
@RequestMapping("/app/pet/breed")
public class AppPetBreedController {

    @Resource
    private PetBreedService petBreedService;

    @Operation(summary = "list")
    @GetMapping("list")
    public Result<List<PetBreedVo>> list(@RequestParam(value = "petType", required = false) String petType) {
        return Result.success(petBreedService.listEnabled(petType));
    }

    @Operation(summary = "listPost")
    @PostMapping("list")
    public Result<List<PetBreedVo>> listPost(@RequestBody(required = false) PetBreedListBo bo) {
        String petType = bo == null ? null : bo.getPetType();
        return Result.success(petBreedService.listEnabled(petType));
    }
}
