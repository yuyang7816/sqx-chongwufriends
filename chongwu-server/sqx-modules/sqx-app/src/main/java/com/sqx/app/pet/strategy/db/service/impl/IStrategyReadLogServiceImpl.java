package com.sqx.app.pet.strategy.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.strategy.db.mapper.StrategyReadLogMapper;
import com.sqx.app.pet.strategy.db.service.IStrategyReadLogService;
import com.sqx.app.pet.strategy.domain.entity.StrategyReadLog;
import org.springframework.stereotype.Service;

@Service
public class IStrategyReadLogServiceImpl extends ServiceImpl<StrategyReadLogMapper, StrategyReadLog> implements IStrategyReadLogService {
}
