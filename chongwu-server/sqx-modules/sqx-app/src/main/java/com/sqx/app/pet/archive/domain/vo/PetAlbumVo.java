package com.sqx.app.pet.archive.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "PetAlbumVo")
public class PetAlbumVo implements Serializable {
    private Long id;
    private Long petId;
    private String mediaUrl;
    private Integer mediaType;
    private Integer sort;
}
