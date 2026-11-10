package com.sqx.app.pet.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "NotificationConfigVo")
public class NotificationConfigVo implements Serializable {
    private Integer pushEnabled;
    private Integer smsEnabled;
}
