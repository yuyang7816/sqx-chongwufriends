package com.sqx.app.pet.feed.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "FeedOrderUpdateBo")
public class FeedOrderUpdateBo {
    private Long id;
}
