package com.sqx.app.pet.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.mall.domain.bo.*;
import com.sqx.app.pet.mall.domain.vo.ExchangeRecordVo;
import com.sqx.app.pet.mall.domain.vo.MallOrderVo;

public interface MallOrderService {
    MallOrderVo createOrder(Long userId, MallOrderCreateBo bo);
    MallOrderVo createExchangeOrder(Long userId, MallOrderCreateBo bo);
    void cancelOrder(Long userId, MallOrderCancelBo bo);
    IPage<MallOrderVo> listMyOrders(Long userId, MallOrderPageBo bo);
    IPage<ExchangeRecordVo> listExchangeRecords(Long userId, MallOrderPageBo bo);
    MallOrderVo info(Long userId, MallOrderInfoBo bo);

    IPage<MallOrderVo> adminPage(MallOrderPageBo bo);

    MallOrderVo adminInfo(MallOrderInfoBo bo);

    void adminCancel(MallOrderInfoBo bo);

    void adminRefund(MallOrderRefundBo bo);

    void adminUpdateExchangeStatus(MallOrderExchangeStatusBo bo);
}
