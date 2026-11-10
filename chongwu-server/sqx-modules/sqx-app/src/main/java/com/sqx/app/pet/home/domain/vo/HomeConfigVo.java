package com.sqx.app.pet.home.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "首页配置")
public class HomeConfigVo implements Serializable {
    private HomeSocialEntryVo xhs;
    private HomeSocialEntryVo community;
}
