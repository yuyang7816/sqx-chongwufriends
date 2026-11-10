package com.sqx.app.pet.mall.db.service.impl;

import com.sqx.app.pet.mall.db.mapper.MallSpuMapper;
import com.sqx.app.pet.mall.db.service.IMallSpuService;
import com.sqx.app.pet.mall.domain.entity.MallSpu;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IMallSpuServiceImpl extends ServiceImpl<MallSpuMapper, MallSpu> implements IMallSpuService {
}
