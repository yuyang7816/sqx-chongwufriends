package com.sqx.app.pet.payment.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.payment.db.mapper.PayCallbackLogMapper;
import com.sqx.app.pet.payment.db.service.IPayCallbackLogService;
import com.sqx.app.pet.payment.domain.entity.PayCallbackLog;
import org.springframework.stereotype.Service;

@Service
public class IPayCallbackLogServiceImpl extends ServiceImpl<PayCallbackLogMapper, PayCallbackLog> implements IPayCallbackLogService {
}
