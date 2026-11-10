package com.sqx.app.pet.community.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.community.db.mapper.CommunityViewLogMapper;
import com.sqx.app.pet.community.db.service.ICommunityViewLogService;
import com.sqx.app.pet.community.domain.entity.CommunityViewLog;
import org.springframework.stereotype.Service;

@Service
public class ICommunityViewLogServiceImpl extends ServiceImpl<CommunityViewLogMapper, CommunityViewLog> implements ICommunityViewLogService {
}
