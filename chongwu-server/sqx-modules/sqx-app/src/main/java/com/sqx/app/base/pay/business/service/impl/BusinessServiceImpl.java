package com.sqx.app.base.pay.business.service.impl;

import com.sqx.app.base.pay.business.service.BusinessService;
import com.sqx.app.base.pay.domain.BusinessParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author chengming
 * @since 2025/4/21
 */
@Slf4j
@Service
public class BusinessServiceImpl implements BusinessService {

    @Override
    public void process(BusinessParam param) {
        log.info("支付业务处理");

    }
}
