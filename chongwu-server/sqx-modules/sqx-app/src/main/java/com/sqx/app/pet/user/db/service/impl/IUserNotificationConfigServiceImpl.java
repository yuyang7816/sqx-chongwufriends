package com.sqx.app.pet.user.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.user.db.mapper.UserNotificationConfigMapper;
import com.sqx.app.pet.user.db.service.IUserNotificationConfigService;
import com.sqx.app.pet.user.domain.entity.UserNotificationConfig;
import org.springframework.stereotype.Service;

@Service
public class IUserNotificationConfigServiceImpl extends ServiceImpl<UserNotificationConfigMapper, UserNotificationConfig> implements IUserNotificationConfigService {
}
