package com.sqx.app.pet.adopt.db.service.impl;

import com.sqx.app.pet.adopt.db.mapper.AdoptApplicationMapper;
import com.sqx.app.pet.adopt.db.service.IAdoptApplicationService;
import com.sqx.app.pet.adopt.domain.entity.AdoptApplication;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IAdoptApplicationServiceImpl extends ServiceImpl<AdoptApplicationMapper, AdoptApplication> implements IAdoptApplicationService {
}
