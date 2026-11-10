package com.sqx.app.pet.archive.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "PetArchivePageBo")
public class PetArchivePageBo extends PageParam {
    private Long storeId;
    private String name;
    private String petType;
    private Integer adoptable;
    private Integer auditStatus;
}
