package com.sqx.app.pet.user.service.impl;

import com.sqx.app.base.user.domain.bo.UpdateUserBO;
import com.sqx.app.base.user.service.UserService;
import com.sqx.app.pet.common.service.SensitiveWordService;
import com.sqx.app.pet.user.db.service.IUserCancelApplyService;
import com.sqx.app.pet.user.db.service.IUserNotificationConfigService;
import com.sqx.app.pet.user.db.service.IWecomConfigService;
import com.sqx.app.pet.user.domain.bo.CancelApplyBo;
import com.sqx.app.pet.user.domain.bo.NotificationConfigBo;
import com.sqx.app.pet.user.domain.bo.PetUserUpdateBo;
import com.sqx.app.pet.user.domain.entity.UserCancelApply;
import com.sqx.app.pet.user.domain.entity.UserNotificationConfig;
import com.sqx.app.pet.user.domain.entity.WecomConfig;
import com.sqx.app.pet.user.domain.vo.NotificationConfigVo;
import com.sqx.app.pet.user.domain.vo.WecomConfigVo;
import com.sqx.app.pet.user.service.PetUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.db.service.ISysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class PetUserServiceImpl implements PetUserService {

    @Resource
    private IUserNotificationConfigService iUserNotificationConfigService;
    @Resource
    private IWecomConfigService iWecomConfigService;
    @Resource
    private IUserCancelApplyService iUserCancelApplyService;
    @Resource
    private SensitiveWordService sensitiveWordService;
    @Resource
    private UserService userService;
    @Resource
    private ISysConfigService iSysConfigService;

    private static final String DEFAULT_WECOM_TITLE = "和它交个朋友";
    private static final String DEFAULT_WECOM_SUBTITLE = "加入我们的宠物大家庭";
    private static final String DEFAULT_WECOM_BENEFIT =
            "福利早知道|第一时间获取门店优惠、新品信息\n养宠顾问|专业养宠师在线答疑，随时咨询\n宠友社群|和 2000+ 宠友交流养宠心得";

    @Override
    public NotificationConfigVo getNotification(Long userId) {
        UserNotificationConfig config = iUserNotificationConfigService.lambdaQuery()
                .eq(UserNotificationConfig::getUserId, userId).one();
        NotificationConfigVo vo = new NotificationConfigVo();
        if (config != null) {
            vo.setPushEnabled(config.getPushEnabled());
            vo.setSmsEnabled(config.getSmsEnabled());
        } else {
            vo.setPushEnabled(1);
            vo.setSmsEnabled(1);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveNotification(Long userId, NotificationConfigBo bo) {
        UserNotificationConfig config = iUserNotificationConfigService.lambdaQuery()
                .eq(UserNotificationConfig::getUserId, userId).one();
        if (config == null) {
            config = new UserNotificationConfig();
            config.setUserId(userId);
        }
        if (bo.getPushEnabled() != null) {
            config.setPushEnabled(bo.getPushEnabled());
        }
        if (bo.getSmsEnabled() != null) {
            config.setSmsEnabled(bo.getSmsEnabled());
        }
        iUserNotificationConfigService.saveOrUpdate(config);
    }

    @Override
    public WecomConfigVo getWecom() {
        WecomConfig config = iWecomConfigService.lambdaQuery()
                .eq(WecomConfig::getStatus, 1)
                .orderByDesc(WecomConfig::getId)
                .last("limit 1")
                .one();
        WecomConfigVo vo = new WecomConfigVo();
        if (config != null) {
            vo.setQrUrl(config.getQrUrl());
            vo.setWecomId(config.getWecomId());
            vo.setPageTitle(StringUtils.defaultIfBlank(config.getCommunityTitle(), DEFAULT_WECOM_TITLE));
            vo.setPageSubtitle(StringUtils.defaultIfBlank(config.getCommunitySubtitle(), DEFAULT_WECOM_SUBTITLE));
        } else {
            vo.setPageTitle(DEFAULT_WECOM_TITLE);
            vo.setPageSubtitle(DEFAULT_WECOM_SUBTITLE);
        }
        vo.setServiceLink(readSysConfig("wecom_service_link", ""));
        vo.setBenefitText(readSysConfig("wecom_benefit_text", DEFAULT_WECOM_BENEFIT));
        return vo;
    }

    private String readSysConfig(String key, String defaultValue) {
        SysConfigEntity entity = iSysConfigService.getOne(new LambdaQueryWrapper<SysConfigEntity>()
                .eq(SysConfigEntity::getParamKey, key)
                .last("limit 1"));
        if (entity != null && StringUtils.isNotBlank(entity.getParamValue())) {
            return entity.getParamValue().trim();
        }
        return defaultValue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApply(Long userId, CancelApplyBo bo) {
        UserCancelApply apply = new UserCancelApply();
        apply.setUserId(userId);
        apply.setReason(bo.getReason());
        apply.setStatus(1);
        iUserCancelApplyService.save(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, PetUserUpdateBo bo) {
        if (StringUtils.isNotBlank(bo.getUserName())) {
            sensitiveWordService.checkText(bo.getUserName(), "nickname");
        }
        UpdateUserBO updateUserBO = new UpdateUserBO();
        updateUserBO.setUserId(userId);
        updateUserBO.setUserName(bo.getUserName());
        updateUserBO.setAvatar(bo.getAvatar());
        updateUserBO.setBirthday(bo.getBirthday());
        updateUserBO.setWechat(bo.getWechat());
        userService.update(updateUserBO);
    }
}
