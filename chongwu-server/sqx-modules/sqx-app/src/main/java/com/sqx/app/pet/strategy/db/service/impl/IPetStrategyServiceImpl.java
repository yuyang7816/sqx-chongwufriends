package com.sqx.app.pet.strategy.db.service.impl;

import com.sqx.app.pet.strategy.db.mapper.PetStrategyMapper;
import com.sqx.app.pet.strategy.db.service.IPetStrategyService;
import com.sqx.app.pet.strategy.domain.entity.PetStrategy;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IPetStrategyServiceImpl extends ServiceImpl<PetStrategyMapper, PetStrategy> implements IPetStrategyService {
}
