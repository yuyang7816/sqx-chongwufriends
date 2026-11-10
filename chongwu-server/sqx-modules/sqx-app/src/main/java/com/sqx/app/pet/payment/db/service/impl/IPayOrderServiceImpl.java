package com.sqx.app.pet.payment.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.payment.db.mapper.PayOrderMapper;
import com.sqx.app.pet.payment.db.service.IPayOrderService;
import com.sqx.app.pet.payment.domain.entity.PayOrder;
import org.springframework.stereotype.Service;

@Service
public class IPayOrderServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements IPayOrderService {
}
