package com.sqx.app.pet.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "WecomConfigVo")
public class WecomConfigVo implements Serializable {
    private String qrUrl;
    private String wecomId;
    private String pageTitle;
    private String pageSubtitle;
    private String serviceLink;
    private String benefitText;
}
