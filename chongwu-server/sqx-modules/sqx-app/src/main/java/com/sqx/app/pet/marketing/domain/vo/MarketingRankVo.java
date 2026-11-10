package com.sqx.app.pet.marketing.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "MarketingRankVo")
public class MarketingRankVo implements Serializable {
    private Long petId;
    private String petName;
    private String avatarUrl;
    private Integer likeCount;
    private Integer rankNo;
    private String storeName;
}
