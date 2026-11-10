package com.sqx.app.pet.adopt.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "领养申请提交")
public class AdoptApplicationSaveBo {

    @NotNull
    @Schema(description = "宠物ID")
    private Long petId;

    @NotBlank
    @Schema(description = "真实姓名")
    private String realName;

    @NotBlank
    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "领养理由")
    private String reason;

    @Schema(description = "微信号")
    private String wechat;

    @Schema(description = "居住条件")
    private String housing;

    @Schema(description = "是否有养宠经验")
    private Boolean hasExperience;

    @Schema(description = "是否有其他宠物")
    private Boolean hasOtherPet;

    @Schema(description = "其他宠物说明")
    private String otherPetIntro;

    @Schema(description = "工作状态")
    private String workStatus;

    @Schema(description = "每日陪伴时间")
    private String companionTime;

    @Schema(description = "家庭情况")
    private String familyStatus;

    @Schema(description = "养宠挑战")
    private String challenge;

    @Schema(description = "希望领养宠物照片")
    private String petImage;

    @Schema(description = "希望领养宠物名称")
    private String petName;

    @Schema(description = "表单扩展JSON")
    private String formJson;

    @Schema(description = "幂等键")
    private String idempotentKey;
}
