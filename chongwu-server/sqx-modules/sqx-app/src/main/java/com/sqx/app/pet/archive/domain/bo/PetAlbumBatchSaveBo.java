package com.sqx.app.pet.archive.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "PetAlbumBatchSaveBo")
public class PetAlbumBatchSaveBo {
    @NotNull(message = "宠物ID不能为空")
    private Long petId;
    @Schema(description = "随拍相册 URL 列表，按顺序排列")
    private List<String> mediaUrls;
}
