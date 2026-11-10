package com.sqx.app.pet.archive.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("pet_album")
public class PetAlbum implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField("pet_id")
    private Long petId;
    @TableField("media_url")
    private String mediaUrl;
    @TableField("media_type")
    private Integer mediaType;
    @TableField("sort")
    private Integer sort;
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
