package com.sqx.app.pet.adopt.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "领养申请")
public class AdoptApplicationVo implements Serializable {
    private Long id;
    private Long userId;
    private Long petId;
    private String petName;
    private String realName;
    private String phone;
    private String city;
    private String address;
    private String reason;
    private Integer status;
    private String rejectReason;
    private String statusLabel;
    private String petImage;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
