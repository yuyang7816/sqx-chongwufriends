package com.sqx.app.pet.marketing.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("marketing_rank_record")
public class MarketingRankRecord implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("period_id")
    private Long periodId;
    @TableField("pet_id")
    private Long petId;
    @TableField("like_count")
    private Integer likeCount;
    @TableField("rank_no")
    private Integer rankNo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
