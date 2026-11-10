package com.sqx.app.pet.home.service.impl;

import com.sqx.app.pet.home.domain.bo.HomeSocialUpdateBo;
import com.sqx.app.pet.home.domain.vo.HomeConfigVo;
import com.sqx.app.pet.home.domain.vo.HomeSocialEntryVo;
import com.sqx.app.pet.home.service.HomeConfigService;
import com.sqx.app.pet.user.db.service.IWecomConfigService;
import com.sqx.app.pet.user.domain.entity.WecomConfig;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.db.service.ISysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class HomeConfigServiceImpl implements HomeConfigService {

    @Resource
    private IWecomConfigService iWecomConfigService;
    @Resource
    private ISysConfigService iSysConfigService;

    private static final String WECOM_SERVICE_LINK_KEY = "wecom_service_link";
    private static final String WECOM_BENEFIT_TEXT_KEY = "wecom_benefit_text";
    /** 系统配置-图片配置：首页小红书二维码 */
    private static final String HOME_XHS_QR_KEY = "home_xhs_qr_url";

    @Override
    public HomeConfigVo getConfig() {
        WecomConfig config = loadActiveConfig();
        HomeConfigVo vo = new HomeConfigVo();
        vo.setXhs(buildXhs(config));
        vo.setCommunity(buildCommunity(config));
        return vo;
    }

    @Override
    public HomeSocialEntryVo getSocialAdmin() {
        WecomConfig config = loadActiveConfig();
        HomeSocialEntryVo vo = new HomeSocialEntryVo();
        if (config != null) {
            vo.setTitle(config.getCommunityTitle());
            vo.setSubtitle(config.getCommunitySubtitle());
            vo.setJumpUrl("/packageA/wecom/index");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSocial(HomeSocialUpdateBo bo) {
        WecomConfig config = loadActiveConfig();
        if (config == null) {
            config = new WecomConfig();
            config.setStatus(1);
        }
        if (bo.getQrUrl() != null) {
            config.setQrUrl(bo.getQrUrl());
        }
        if (bo.getWecomId() != null) {
            config.setWecomId(bo.getWecomId());
        }
        if (bo.getXhsTitle() != null) {
            config.setXhsTitle(bo.getXhsTitle());
        }
        if (bo.getXhsSubtitle() != null) {
            config.setXhsSubtitle(bo.getXhsSubtitle());
        }
        if (bo.getXhsJumpUrl() != null) {
            config.setXhsJumpUrl(bo.getXhsJumpUrl());
        }
        if (bo.getCommunityTitle() != null) {
            config.setCommunityTitle(bo.getCommunityTitle());
        }
        if (bo.getCommunitySubtitle() != null) {
            config.setCommunitySubtitle(bo.getCommunitySubtitle());
        }
        iWecomConfigService.saveOrUpdate(config);
        if (bo.getServiceLink() != null) {
            upsertSysConfig(WECOM_SERVICE_LINK_KEY, bo.getServiceLink(), "企微客服链接");
        }
        if (bo.getBenefitText() != null) {
            upsertSysConfig(WECOM_BENEFIT_TEXT_KEY, bo.getBenefitText(), "企微页权益说明");
        }
    }

    private void upsertSysConfig(String paramKey, String paramValue, String remark) {
        SysConfigEntity entity = iSysConfigService.getOne(new LambdaQueryWrapper<SysConfigEntity>()
                .eq(SysConfigEntity::getParamKey, paramKey)
                .last("limit 1"));
        if (entity == null) {
            entity = new SysConfigEntity();
            entity.setParamKey(paramKey);
            entity.setParamType("weixin");
            entity.setParamTypeDesc("企微");
            entity.setStatus("1");
            entity.setRemark(remark);
        }
        entity.setParamValue(paramValue);
        if (entity.getId() == null) {
            iSysConfigService.save(entity);
        } else {
            iSysConfigService.updateById(entity);
        }
        iSysConfigService.loadSysConfig();
    }

    private WecomConfig loadActiveConfig() {
        return iWecomConfigService.lambdaQuery()
                .eq(WecomConfig::getStatus, 1)
                .orderByDesc(WecomConfig::getId)
                .last("limit 1")
                .one();
    }

    private HomeSocialEntryVo buildXhs(WecomConfig config) {
        HomeSocialEntryVo vo = new HomeSocialEntryVo();
        vo.setTitle(config != null && StringUtils.isNotBlank(config.getXhsTitle()) ? config.getXhsTitle() : "关注小红书");
        vo.setSubtitle(config != null && StringUtils.isNotBlank(config.getXhsSubtitle()) ? config.getXhsSubtitle() : "获取最新动态");
        vo.setJumpUrl(config != null && StringUtils.isNotBlank(config.getXhsJumpUrl()) ? config.getXhsJumpUrl() : "");
        vo.setQrUrl(readSysConfigValue(HOME_XHS_QR_KEY));
        return vo;
    }

    private HomeSocialEntryVo buildCommunity(WecomConfig config) {
        HomeSocialEntryVo vo = new HomeSocialEntryVo();
        vo.setTitle(config != null && StringUtils.isNotBlank(config.getCommunityTitle()) ? config.getCommunityTitle() : "加入社群");
        vo.setSubtitle(config != null && StringUtils.isNotBlank(config.getCommunitySubtitle()) ? config.getCommunitySubtitle() : "获取养宠咨询");
        vo.setJumpUrl("/packageA/wecom/index");
        return vo;
    }

    private String readSysConfigValue(String key) {
        SysConfigEntity entity = iSysConfigService.getOne(new LambdaQueryWrapper<SysConfigEntity>()
                .eq(SysConfigEntity::getParamKey, key)
                .last("limit 1"));
        return entity != null && StringUtils.isNotBlank(entity.getParamValue()) ? entity.getParamValue().trim() : "";
    }
}
