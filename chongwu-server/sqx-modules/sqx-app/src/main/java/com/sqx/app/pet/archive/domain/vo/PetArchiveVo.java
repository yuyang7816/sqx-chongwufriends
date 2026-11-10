package com.sqx.app.pet.archive.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "PetArchiveVo")
public class PetArchiveVo implements Serializable {
    private Long id;
    private Long storeId;
    @Schema(description = "门店名称")
    private String storeName;
    private String name;
    private String petType;
    private String breed;
    private String gender;
    private String ageDesc;
    private String weightDesc;
    private String petCode;
    private String levelDesc;
    private String statusLabel;
    private String description;
    private String goodAt;
    private String avoidItems;
    private String tags;
    private String avatarUrl;
    @Schema(description = "随拍相册")
    private List<String> dailyAlbum;
    @Schema(description = "社群相册")
    private List<String> socialAlbum;
    @Schema(description = "随拍相册(兼容字段)")
    private List<String> albumImages;
    @Schema(description = "社群相册(兼容字段)")
    private List<String> communityImages;
    private Integer likeCount;
    private Integer adoptable;
    private Integer auditStatus;
    private String rejectReason;
    private Integer sort;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
