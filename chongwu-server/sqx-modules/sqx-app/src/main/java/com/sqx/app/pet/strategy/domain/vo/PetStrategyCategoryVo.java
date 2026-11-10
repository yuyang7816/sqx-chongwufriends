package com.sqx.app.pet.strategy.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "PetStrategyCategoryVo")
public class PetStrategyCategoryVo implements Serializable {
    private Long id;
    private String name;
    private Integer sort;
    private Integer status;
}
