package com.sqx.app.pet.mall.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.mall.db.mapper.MallOrderMapper;
import com.sqx.app.pet.mall.db.service.IMallOrderService;
import com.sqx.app.pet.mall.domain.entity.MallOrder;
import org.springframework.stereotype.Service;

@Service
public class IMallOrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder> implements IMallOrderService {
}
