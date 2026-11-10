package com.sqx.app.pet.payment.service.impl;

import com.sqx.app.pet.payment.db.service.IPayCallbackLogService;
import com.sqx.app.pet.payment.domain.entity.PayCallbackLog;
import com.sqx.app.pet.payment.service.PayCallbackLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PayCallbackLogServiceImpl implements PayCallbackLogService {

    @Resource
    private IPayCallbackLogService iPayCallbackLogService;

    @Override
    public void logCallback(String orderNo, String channel, String tradeType, String requestBody, String responseBody, Integer status) {
        PayCallbackLog log = new PayCallbackLog();
        log.setOrderNo(orderNo);
        log.setChannel(channel);
        log.setTradeType(tradeType);
        log.setRequestBody(requestBody);
        log.setResponseBody(responseBody);
        log.setStatus(status);
        iPayCallbackLogService.save(log);
    }
}
