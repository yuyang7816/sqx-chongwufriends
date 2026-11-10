package com.sqx.app.base.messages.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "批量标记已读")
public class MessagesReadAllBo {
    @Schema(description = "消息类型: 1=系统公告, 2=用户消息，空=全部")
    private Integer type;
}
