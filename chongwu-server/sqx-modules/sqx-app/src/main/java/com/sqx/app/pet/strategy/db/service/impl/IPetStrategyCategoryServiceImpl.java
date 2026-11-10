package com.sqx.app.pet.strategy.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.strategy.db.mapper.PetStrategyCategoryMapper;
import com.sqx.app.pet.strategy.db.service.IPetStrategyCategoryService;
import com.sqx.app.pet.strategy.domain.entity.PetStrategyCategory;
import org.springframework.stereotype.Service;

@Service
public class IPetStrategyCategoryServiceImpl extends ServiceImpl<PetStrategyCategoryMapper, PetStrategyCategory> implements IPetStrategyCategoryService {
}
