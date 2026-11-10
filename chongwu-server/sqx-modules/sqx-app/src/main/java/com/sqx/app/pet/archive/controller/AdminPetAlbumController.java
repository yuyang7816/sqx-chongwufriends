package com.sqx.app.pet.archive.controller;

import com.sqx.app.pet.archive.domain.bo.PetAlbumBatchSaveBo;
import com.sqx.app.pet.archive.domain.bo.PetArchiveInfoBo;
import com.sqx.app.pet.archive.domain.vo.PetAlbumVo;
import com.sqx.app.pet.archive.service.PetArchiveService;
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

@Tag(name = "Admin-Pet Album", description = "宠物随拍相册管理")
@RestController
@RequestMapping("/admin/pet/archive/album")
public class AdminPetAlbumController {

    @Resource
    private PetArchiveService petArchive;

    @Operation(summary = "随拍相册列表")
    @PostMapping("list")
    public Result<List<PetAlbumVo>> list(@RequestBody @Validated PetArchiveInfoBo bo) {
        return Result.success(petArchive.listDailyAlbum(bo.getId()));
    }

    @Operation(summary = "批量保存随拍相册")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated PetAlbumBatchSaveBo bo) {
        petArchive.saveDailyAlbum(bo);
        return Result.success();
    }
}
