package com.sqx.app.pet.home.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "首页社群配置更新")
public class HomeSocialUpdateBo {
    private String qrUrl;
    private String wecomId;
    private String xhsTitle;
    private String xhsSubtitle;
    private String xhsJumpUrl;
    private String communityTitle;
    private String communitySubtitle;
    private String serviceLink;
    private String benefitText;
}
