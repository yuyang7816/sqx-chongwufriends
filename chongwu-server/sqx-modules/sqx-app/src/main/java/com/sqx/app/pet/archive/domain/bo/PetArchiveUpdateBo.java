package com.sqx.app.pet.archive.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "PetArchiveUpdateBo")
public class PetArchiveUpdateBo {
    @NotNull(message = "档案ID不能为空")
    private Long id;
    @NotNull(message = "所属门店不能为空")
    private Long storeId;
    @NotBlank(message = "宠物昵称不能为空")
    private String name;
    @NotBlank(message = "宠物类型不能为空")
    private String petType;
    @NotBlank(message = "品种不能为空")
    private String breed;
    private String gender;
    @NotBlank(message = "年龄不能为空")
    private String ageDesc;
    private String weightDesc;
    private String petCode;
    private String levelDesc;
    private String statusLabel;
    private String description;
    private String goodAt;
    private String avoidItems;
    private String tags;
    @NotBlank(message = "封面图不能为空")
    private String avatarUrl;
    @Schema(description = "随拍相册 URL 列表")
    private List<String> dailyAlbumUrls;
    private Integer likeCount;
    private Integer adoptable;
    private Integer auditStatus;
    private String rejectReason;
    private Integer sort;
}
