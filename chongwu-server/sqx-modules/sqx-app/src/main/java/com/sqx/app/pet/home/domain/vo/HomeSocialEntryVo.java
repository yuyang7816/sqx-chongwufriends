package com.sqx.app.pet.home.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "首页社群入口")
public class HomeSocialEntryVo implements Serializable {
    private String title;
    private String subtitle;
    private String jumpUrl;
    @Schema(description = "二维码图片地址（小红书入口用）")
    private String qrUrl;
}
