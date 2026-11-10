package com.sqx.app.pet.adopt.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AdoptApplicationPageBo")
public class AdoptApplicationPageBo extends PageParam {
    private Integer status;
    private Long userId;
    private Long petId;
    private String phone;
}
