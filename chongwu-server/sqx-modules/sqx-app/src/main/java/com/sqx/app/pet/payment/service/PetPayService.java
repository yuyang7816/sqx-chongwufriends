package com.sqx.app.pet.payment.service;

import com.sqx.app.pet.payment.domain.bo.PetPayCreateBo;
import com.sqx.app.pet.payment.domain.bo.PetPayQueryBo;
import com.sqx.app.pet.payment.domain.vo.PetPayChannelVo;
import com.sqx.app.pet.payment.domain.vo.PetPayOrderVo;

public interface PetPayService {
    PetPayChannelVo getChannels();

    PetPayOrderVo createPayOrder(Long userId, PetPayCreateBo bo);

    PetPayOrderVo queryStatus(Long userId, PetPayQueryBo bo);

    String getAlipayWapHtml(String payNo);
}
