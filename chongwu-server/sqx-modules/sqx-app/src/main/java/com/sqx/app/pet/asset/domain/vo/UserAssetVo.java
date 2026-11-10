package com.sqx.app.pet.asset.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "UserAssetVo")
public class UserAssetVo implements Serializable {
    private Long id;
    private Long userId;
    private Integer points;
    private Integer coins;
    private Integer loveValue;
    private Integer flowers;
    private Integer level;
    private String levelName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
