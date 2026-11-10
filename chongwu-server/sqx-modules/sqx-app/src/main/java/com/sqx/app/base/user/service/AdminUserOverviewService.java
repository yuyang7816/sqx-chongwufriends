package com.sqx.app.base.user.service;

import com.sqx.app.base.user.domain.vo.AdminUserOverviewVo;

public interface AdminUserOverviewService {
    AdminUserOverviewVo overview(Long userId);
}
