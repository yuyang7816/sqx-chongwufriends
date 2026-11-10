package com.sqx.app.pet.store.db.service.impl;

import com.sqx.app.pet.store.db.mapper.PetStoreMapper;
import com.sqx.app.pet.store.db.service.IPetStoreService;
import com.sqx.app.pet.store.domain.entity.PetStore;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IPetStoreServiceImpl extends ServiceImpl<PetStoreMapper, PetStore> implements IPetStoreService {
}
