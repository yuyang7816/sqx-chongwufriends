package com.sqx.app.pet.store.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "PetStorePageBo")
public class PetStorePageBo extends PageParam {
    private Long cityId;
    private String name;
    private Integer businessStatus;
}
