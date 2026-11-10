package com.sqx.app.pet.mall.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.mall.db.mapper.MallSkuMapper;
import com.sqx.app.pet.mall.db.service.IMallSkuService;
import com.sqx.app.pet.mall.domain.entity.MallSku;
import org.springframework.stereotype.Service;

@Service
public class IMallSkuServiceImpl extends ServiceImpl<MallSkuMapper, MallSku> implements IMallSkuService {
}
