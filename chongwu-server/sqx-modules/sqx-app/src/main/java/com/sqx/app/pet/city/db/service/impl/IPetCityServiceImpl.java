package com.sqx.app.pet.city.db.service.impl;

import com.sqx.app.pet.city.db.mapper.PetCityMapper;
import com.sqx.app.pet.city.db.service.IPetCityService;
import com.sqx.app.pet.city.domain.entity.PetCity;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IPetCityServiceImpl extends ServiceImpl<PetCityMapper, PetCity> implements IPetCityService {
}
