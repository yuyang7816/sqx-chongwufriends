package com.sqx.app.pet.feed.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "FeedOrderVo")
public class FeedOrderVo implements Serializable {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long petId;
    private String productName;
    private BigDecimal amount;
    private Integer quantity;
    private String message;
    private Integer status;
    private String statusText;
    private String date;
    private String pet;
    private String item;
    private String note;
    private String videoUrl;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
