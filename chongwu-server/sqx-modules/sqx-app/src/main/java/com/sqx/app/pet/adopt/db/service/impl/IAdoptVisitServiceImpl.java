package com.sqx.app.pet.adopt.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.adopt.db.mapper.AdoptVisitMapper;
import com.sqx.app.pet.adopt.db.service.IAdoptVisitService;
import com.sqx.app.pet.adopt.domain.entity.AdoptVisit;
import org.springframework.stereotype.Service;

@Service
public class IAdoptVisitServiceImpl extends ServiceImpl<AdoptVisitMapper, AdoptVisit> implements IAdoptVisitService {
}
