package com.sqx.app.pet.marketing.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.marketing.db.mapper.MarketingAssistMapper;
import com.sqx.app.pet.marketing.db.service.IMarketingAssistService;
import com.sqx.app.pet.marketing.domain.entity.MarketingAssist;
import org.springframework.stereotype.Service;

@Service
public class IMarketingAssistServiceImpl extends ServiceImpl<MarketingAssistMapper, MarketingAssist> implements IMarketingAssistService {
}
