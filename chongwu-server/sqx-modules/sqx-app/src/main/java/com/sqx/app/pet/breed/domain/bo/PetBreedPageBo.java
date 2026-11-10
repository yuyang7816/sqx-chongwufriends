package com.sqx.app.pet.breed.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "PetBreedPageBo")
public class PetBreedPageBo extends PageParam {
    private String petType;
    private String name;
    private Integer status;
}
