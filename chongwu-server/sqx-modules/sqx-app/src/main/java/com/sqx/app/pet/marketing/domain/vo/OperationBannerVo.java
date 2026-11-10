package com.sqx.app.pet.marketing.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "OperationBannerVo")
public class OperationBannerVo implements Serializable {
    private Long id;
    private String name;
    private String subTitle;
    private String imageUrl;
    private String jumpUrl;
    private String positionCode;
    private Integer sort;
    private Integer status;
    private Integer clickCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
