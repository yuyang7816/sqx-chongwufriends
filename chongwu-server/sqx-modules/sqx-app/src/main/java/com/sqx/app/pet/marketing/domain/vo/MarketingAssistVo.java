package com.sqx.app.pet.marketing.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "MarketingAssistVo")
public class MarketingAssistVo implements Serializable {
    private Long id;
    private Long periodId;
    private Long petId;
    private String assistType;
    private String target;
    private String type;
    private String time;
    private String status;
    private String petName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
