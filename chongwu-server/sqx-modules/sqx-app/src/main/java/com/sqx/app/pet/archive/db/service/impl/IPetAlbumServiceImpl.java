package com.sqx.app.pet.archive.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.archive.db.mapper.PetAlbumMapper;
import com.sqx.app.pet.archive.db.service.IPetAlbumService;
import com.sqx.app.pet.archive.domain.entity.PetAlbum;
import org.springframework.stereotype.Service;

@Service
public class IPetAlbumServiceImpl extends ServiceImpl<PetAlbumMapper, PetAlbum> implements IPetAlbumService {
}
