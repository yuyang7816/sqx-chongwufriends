package com.sqx.app.pet.island.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "IslandPetUpdateBo")
public class IslandPetUpdateBo {
    private Long id;
    private Integer hunger;
    private Integer clean;
    private Integer mood;
    private Integer vitality;
    private Integer intimacy;
    private Integer dailyProgress;
    private Long residentPetId;
    private String residentPetIds;
    private java.time.LocalDateTime feedCooldownUntil;
    private Integer foodBowlFilled;
    private Integer waterBowlFilled;
}
