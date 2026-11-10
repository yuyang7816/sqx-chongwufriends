package com.sqx.app.pet.home.controller;

import com.sqx.app.pet.home.domain.bo.HomeSocialUpdateBo;
import com.sqx.app.pet.home.domain.vo.HomeConfigVo;
import com.sqx.app.pet.home.service.HomeConfigService;
import com.sqx.app.pet.user.domain.entity.WecomConfig;
import com.sqx.app.pet.user.db.service.IWecomConfigService;
import com.sqx.common.core.domain.Result;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.db.service.ISysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Admin-首页配置", description = "Admin-首页配置")
@RestController
@RequestMapping("/admin/pet/home")
public class AdminPetHomeController {

    @Resource
    private HomeConfigService homeConfigService;
    @Resource
    private IWecomConfigService iWecomConfigService;
    @Resource
    private ISysConfigService iSysConfigService;

    private static final String WECOM_SERVICE_LINK_KEY = "wecom_service_link";
    private static final String WECOM_BENEFIT_TEXT_KEY = "wecom_benefit_text";

    @Operation(summary = "社群入口配置详情")
    @PostMapping("social/info")
    public Result<Map<String, Object>> socialInfo() {
        WecomConfig config = iWecomConfigService.lambdaQuery()
                .eq(WecomConfig::getStatus, 1)
                .orderByDesc(WecomConfig::getId)
                .last("limit 1")
                .one();
        Map<String, Object> map = new HashMap<>();
        if (config != null) {
            map.put("qrUrl", config.getQrUrl());
            map.put("wecomId", config.getWecomId());
            map.put("xhsTitle", config.getXhsTitle());
            map.put("xhsSubtitle", config.getXhsSubtitle());
            map.put("xhsJumpUrl", config.getXhsJumpUrl());
            map.put("communityTitle", config.getCommunityTitle());
            map.put("communitySubtitle", config.getCommunitySubtitle());
        }
        map.put("serviceLink", readConfig(WECOM_SERVICE_LINK_KEY));
        map.put("benefitText", readConfig(WECOM_BENEFIT_TEXT_KEY));
        HomeConfigVo preview = homeConfigService.getConfig();
        map.put("preview", preview);
        return Result.success(map);
    }

    @Operation(summary = "更新社群入口配置")
    @PostMapping("social/update")
    public Result<Void> socialUpdate(@RequestBody HomeSocialUpdateBo bo) {
        homeConfigService.updateSocial(bo);
        return Result.success();
    }

    private String readConfig(String key) {
        SysConfigEntity entity = iSysConfigService.getOne(new LambdaQueryWrapper<SysConfigEntity>()
                .eq(SysConfigEntity::getParamKey, key)
                .last("limit 1"));
        return entity != null && StringUtils.isNotBlank(entity.getParamValue()) ? entity.getParamValue().trim() : "";
    }
}
