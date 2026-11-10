package com.sqx.app.pet.strategy.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "PetStrategyVo")
public class PetStrategyVo implements Serializable {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String subTitle;
    private String tag;
    private String coverUrl;
    private String content;
    private Integer viewCount;
    private Integer likeCount;
    private Integer sort;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
