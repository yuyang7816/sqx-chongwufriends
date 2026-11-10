package com.sqx.app.pet.island.db.service.impl;

import com.sqx.app.pet.island.db.mapper.IslandPetMapper;
import com.sqx.app.pet.island.db.service.IIslandPetService;
import com.sqx.app.pet.island.domain.entity.IslandPet;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IIslandPetServiceImpl extends ServiceImpl<IslandPetMapper, IslandPet> implements IIslandPetService {
}
