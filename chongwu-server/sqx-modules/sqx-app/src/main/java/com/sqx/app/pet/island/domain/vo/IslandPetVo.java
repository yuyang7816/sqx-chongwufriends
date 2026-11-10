package com.sqx.app.pet.island.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "IslandPetVo")
public class IslandPetVo implements Serializable {
    private Long id;
    private Long userId;
    private Integer hunger;
    private Integer clean;
    private Integer mood;
    private Integer vitality;
    private Integer intimacy;
    private Integer intimacyLevel;
    private Integer intimacyPercent;
    /** 今日进度 0-100，每日初始由系统配置决定 */
    private Integer dailyProgress;
    private Long residentPetId;
    private String residentPetIds;
    private String petName;
    private String petAvatar;
    private String petType;
    private String breed;
    private String gender;
    private String statusText;
    private String moodEmoji;
    private Integer feedCooldownSeconds;
    private String feedCooldownText;
    /** 冷却中且双碗都喂过时为 false；否则只要还有空碗就可继续喂 */
    private Boolean canFeed;
    private Boolean canFeedFood;
    private Boolean canFeedWater;
    private Integer loveValue;
    private Integer loveGain;
    private Boolean foodBowlFilled;
    private Boolean waterBowlFilled;
    private String lastFeedType;
    private Integer coins;
    private Integer points;
    private Integer flowers;
    private Integer level;
    private String levelName;
    private List<IslandResidentVo> residents;
    private List<IslandActionVo> leftActions;
    private List<IslandActionVo> rightActions;
    private String weatherText;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime feedCooldownUntil;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
