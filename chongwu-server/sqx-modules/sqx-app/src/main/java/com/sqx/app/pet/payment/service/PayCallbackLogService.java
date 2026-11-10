package com.sqx.app.pet.payment.service;

public interface PayCallbackLogService {
    void logCallback(String orderNo, String channel, String tradeType, String requestBody, String responseBody, Integer status);
}
