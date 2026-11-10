package com.sqx.app.pet.asset.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "UserAssetUpdateBo")
public class UserAssetUpdateBo {
    private Long id;
}
