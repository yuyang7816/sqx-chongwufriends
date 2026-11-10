package com.sqx.app.pet.island.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "保存常驻宠物")
public class IslandResidentSaveBo {
    @Size(max = 1, message = "最多选择1只常驻宠物")
    private List<Long> petIds;
}
