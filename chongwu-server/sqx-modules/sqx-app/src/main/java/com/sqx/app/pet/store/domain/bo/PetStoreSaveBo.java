package com.sqx.app.pet.store.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "PetStoreSaveBo")
public class PetStoreSaveBo {
    private Long cityId;
    private String name;
    private String address;
    private String phone;
    private String tone;
    private String coverUrl;
    private Integer businessStatus;
    private Integer featured;
    private Integer sort;
}
