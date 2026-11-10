package com.sqx.app.pet.user.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "通知配置")
public class NotificationConfigBo {
    private Integer pushEnabled;
    private Integer smsEnabled;
}
