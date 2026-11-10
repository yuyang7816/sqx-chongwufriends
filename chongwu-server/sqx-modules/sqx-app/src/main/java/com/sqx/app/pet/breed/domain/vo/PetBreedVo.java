package com.sqx.app.pet.breed.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "PetBreedVo")
public class PetBreedVo implements Serializable {
    private Long id;
    private String petType;
    private String name;
    private Integer sort;
    private Integer status;
}
