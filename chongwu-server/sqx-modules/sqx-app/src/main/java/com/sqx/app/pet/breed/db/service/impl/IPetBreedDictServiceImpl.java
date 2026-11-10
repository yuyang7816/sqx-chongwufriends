package com.sqx.app.pet.breed.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.breed.db.mapper.PetBreedDictMapper;
import com.sqx.app.pet.breed.db.service.IPetBreedDictService;
import com.sqx.app.pet.breed.domain.entity.PetBreedDict;
import org.springframework.stereotype.Service;

@Service
public class IPetBreedDictServiceImpl extends ServiceImpl<PetBreedDictMapper, PetBreedDict> implements IPetBreedDictService {
}
