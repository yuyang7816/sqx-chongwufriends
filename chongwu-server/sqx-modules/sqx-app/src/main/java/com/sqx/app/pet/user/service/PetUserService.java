package com.sqx.app.pet.user.service;

import com.sqx.app.pet.user.domain.bo.CancelApplyBo;
import com.sqx.app.pet.user.domain.bo.NotificationConfigBo;
import com.sqx.app.pet.user.domain.bo.PetUserUpdateBo;
import com.sqx.app.pet.user.domain.vo.NotificationConfigVo;
import com.sqx.app.pet.user.domain.vo.WecomConfigVo;

public interface PetUserService {
    NotificationConfigVo getNotification(Long userId);
    void saveNotification(Long userId, NotificationConfigBo bo);
    WecomConfigVo getWecom();
    void cancelApply(Long userId, CancelApplyBo bo);
    void updateProfile(Long userId, PetUserUpdateBo bo);
}
