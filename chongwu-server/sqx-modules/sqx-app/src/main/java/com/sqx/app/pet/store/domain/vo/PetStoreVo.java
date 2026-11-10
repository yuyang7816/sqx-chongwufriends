package com.sqx.app.pet.store.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "PetStoreVo")
public class PetStoreVo implements Serializable {
    private Long id;
    private Long cityId;
    private String name;
    private String address;
    private String phone;
    private String tone;
    private String coverUrl;
    private Integer businessStatus;
    private Integer featured;
    private Integer sort;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
