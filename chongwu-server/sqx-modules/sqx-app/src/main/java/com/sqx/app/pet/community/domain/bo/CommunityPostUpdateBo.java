package com.sqx.app.pet.community.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "CommunityPostUpdateBo")
public class CommunityPostUpdateBo {
    private Long id;
}
