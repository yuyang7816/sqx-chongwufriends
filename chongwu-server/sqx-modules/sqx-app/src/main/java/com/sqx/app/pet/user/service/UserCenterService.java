package com.sqx.app.pet.user.service;

import com.sqx.app.pet.user.domain.vo.UserCenterSummaryVo;

public interface UserCenterService {
    UserCenterSummaryVo getSummary(Long userId);
}
