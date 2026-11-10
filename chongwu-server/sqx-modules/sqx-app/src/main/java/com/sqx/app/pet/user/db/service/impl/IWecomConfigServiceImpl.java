package com.sqx.app.pet.user.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.user.db.mapper.WecomConfigMapper;
import com.sqx.app.pet.user.db.service.IWecomConfigService;
import com.sqx.app.pet.user.domain.entity.WecomConfig;
import org.springframework.stereotype.Service;

@Service
public class IWecomConfigServiceImpl extends ServiceImpl<WecomConfigMapper, WecomConfig> implements IWecomConfigService {
}
