package com.sqx.app.pet.archive.db.service.impl;

import com.sqx.app.pet.archive.db.mapper.PetArchiveMapper;
import com.sqx.app.pet.archive.db.service.IPetArchiveService;
import com.sqx.app.pet.archive.domain.entity.PetArchive;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IPetArchiveServiceImpl extends ServiceImpl<PetArchiveMapper, PetArchive> implements IPetArchiveService {
}
