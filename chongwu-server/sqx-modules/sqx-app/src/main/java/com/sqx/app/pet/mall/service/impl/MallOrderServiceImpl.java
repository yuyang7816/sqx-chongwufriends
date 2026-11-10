package com.sqx.app.pet.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.support.UserIdResolveHelper;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.common.service.AssetLedgerService;
import com.sqx.app.pet.asset.service.UserAssetService;
import com.sqx.app.pet.asset.domain.vo.UserAssetVo;
import com.sqx.app.pet.mall.db.service.IMallOrderService;
import com.sqx.app.pet.mall.db.service.IMallSkuService;
import com.sqx.app.pet.mall.db.service.IMallSpuService;
import com.sqx.app.pet.mall.domain.bo.MallOrderCancelBo;
import com.sqx.app.pet.mall.domain.bo.MallOrderCreateBo;
import com.sqx.app.pet.mall.domain.bo.MallOrderExchangeStatusBo;
import com.sqx.app.pet.mall.domain.bo.MallOrderInfoBo;
import com.sqx.app.pet.mall.domain.bo.MallOrderPageBo;
import com.sqx.app.pet.mall.domain.bo.MallOrderRefundBo;
import com.sqx.app.pet.mall.domain.entity.MallOrder;
import com.sqx.app.pet.mall.domain.entity.MallSku;
import com.sqx.app.pet.mall.domain.entity.MallSpu;
import com.sqx.app.pet.mall.domain.vo.ExchangeRecordVo;
import com.sqx.app.pet.mall.domain.vo.MallOrderVo;
import com.sqx.app.pet.mall.service.MallOrderService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.GenUtil;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class MallOrderServiceImpl implements MallOrderService {

    @Resource
    private IMallOrderService iMallOrderService;
    @Resource
    private IMallSkuService iMallSkuService;
    @Resource
    private IMallSpuService iMallSpuService;
    @Resource
    private AssetLedgerService assetLedgerService;
    @Resource
    private UserAssetService userAssetService;
    @Resource
    private UserIdResolveHelper userIdResolveHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallOrderVo createOrder(Long userId, MallOrderCreateBo bo) {
        MallSku sku = resolveSku(bo);
        synchronized (("mall_sku_" + sku.getId()).intern()) {
            sku = iMallSkuService.getById(sku.getId());
            if (sku == null) {
                throw new BizException(PetError.INFO_NOT_FOUND);
            }
            int qty = bo.getQuantity() == null ? 1 : bo.getQuantity();
            if (sku.getStock() == null || sku.getStock() < qty) {
                throw new BizException(PetError.STOCK_NOT_ENOUGH);
            }
            sku.setStock(sku.getStock() - qty);
            iMallSkuService.updateById(sku);
        }
        MallSpu spu = iMallSpuService.getById(sku.getSpuId());
        int qty = bo.getQuantity() == null ? 1 : bo.getQuantity();
        MallOrder order = new MallOrder();
        order.setOrderNo(GenUtil.genOrderNo());
        order.setUserId(userId);
        order.setSpuId(sku.getSpuId());
        order.setSkuId(sku.getId());
        order.setQuantity(qty);
        order.setStatus(1);
        order.setPayStatus(1);
        order.setRemark(bo.getRemark());
        if (spu != null) {
            order.setBizType(spu.getBizType() == null ? 1 : spu.getBizType());
            if (Objects.equals(spu.getBizType(), 4)) {
                int pointPrice = sku.getPointPrice() == null ? (spu.getPointPrice() == null ? 0 : spu.getPointPrice()) : sku.getPointPrice();
                int totalPoints = pointPrice * qty;
                assetLedgerService.change(userId, AssetLedgerService.POINTS, totalPoints, 2, "mall_exchange", null, "积分兑换");
                order.setPayType(2);
                order.setPointAmount(totalPoints);
                order.setPayAmount(BigDecimal.ZERO);
                order.setStatus(2);
                order.setPayStatus(3);
            } else {
                BigDecimal price = sku.getPrice() != null ? sku.getPrice() : (spu.getPrice() == null ? BigDecimal.ZERO : spu.getPrice());
                order.setPayAmount(price.multiply(BigDecimal.valueOf(qty)));
                order.setPayType(1);
            }
        }
        iMallOrderService.save(order);
        MallOrderVo vo = new MallOrderVo();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MallOrderVo createExchangeOrder(Long userId, MallOrderCreateBo bo) {
        MallSku sku = resolveSku(bo);
        synchronized (("mall_sku_" + sku.getId()).intern()) {
            sku = iMallSkuService.getById(sku.getId());
            if (sku == null) {
                throw new BizException(PetError.INFO_NOT_FOUND);
            }
            int qty = bo.getQuantity() == null ? 1 : bo.getQuantity();
            if (sku.getStock() == null || sku.getStock() < qty) {
                throw new BizException(PetError.STOCK_NOT_ENOUGH);
            }
            MallSpu spu = iMallSpuService.getById(sku.getSpuId());
            int pointPrice = resolvePointPrice(sku, spu);
            if (pointPrice <= 0) {
                throw new BizException("该商品不支持积分兑换");
            }
            int totalPoints = pointPrice * qty;
            UserAssetVo asset = userAssetService.infoByUserId(userId);
            int balance = asset == null || asset.getPoints() == null ? 0 : asset.getPoints();
            if (balance < totalPoints) {
                throw new BizException(String.format("积分余额不足，当前%d积分，需要%d积分", balance, totalPoints));
            }
            sku.setStock(sku.getStock() - qty);
            iMallSkuService.updateById(sku);

            assetLedgerService.change(userId, AssetLedgerService.POINTS, totalPoints, 2, "mall_exchange", null, "积分兑换");
            MallOrder order = new MallOrder();
            order.setOrderNo(GenUtil.genOrderNo());
            order.setUserId(userId);
            order.setSpuId(sku.getSpuId());
            order.setSkuId(sku.getId());
            order.setQuantity(qty);
            order.setStatus(2);
            order.setPayStatus(3);
            order.setPayType(2);
            order.setPointAmount(totalPoints);
            order.setPayAmount(BigDecimal.ZERO);
            order.setRemark(bo.getRemark());
            order.setBizType(4);
            iMallOrderService.save(order);
            MallOrderVo vo = new MallOrderVo();
            BeanUtils.copyProperties(order, vo);
            return vo;
        }
    }

    private int resolvePointPrice(MallSku sku, MallSpu spu) {
        if (sku.getPointPrice() != null && sku.getPointPrice() > 0) {
            return sku.getPointPrice();
        }
        if (spu != null && spu.getPointPrice() != null && spu.getPointPrice() > 0) {
            return spu.getPointPrice();
        }
        return 0;
    }

    private MallSku resolveSku(MallOrderCreateBo bo) {
        if (bo.getSkuId() != null) {
            MallSku sku = iMallSkuService.getById(bo.getSkuId());
            if (sku != null) {
                return sku;
            }
        }
        if (bo.getSpuId() == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        QueryWrapper<MallSku> qw = new QueryWrapper<>();
        qw.lambda().eq(MallSku::getSpuId, bo.getSpuId());
        if (StringUtils.isNotBlank(bo.getSkuName())) {
            qw.lambda().eq(MallSku::getSkuName, bo.getSkuName());
        }
        qw.lambda().orderByAsc(MallSku::getId).last("limit 1");
        MallSku sku = iMallSkuService.getOne(qw);
        if (sku == null) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        return sku;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, MallOrderCancelBo bo) {
        MallOrder order = iMallOrderService.getById(bo.getId());
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(PetError.ORDER_NOT_FOUND);
        }
        if (!Objects.equals(order.getStatus(), 1)) {
            throw new BizException(PetError.ORDER_NOT_FOUND);
        }
        MallSku sku = iMallSkuService.getById(order.getSkuId());
        if (sku != null) {
            sku.setStock((sku.getStock() == null ? 0 : sku.getStock()) + (order.getQuantity() == null ? 1 : order.getQuantity()));
            iMallSkuService.updateById(sku);
        }
        if (isExchangeOrder(order) && order.getPointAmount() != null && order.getPointAmount() > 0) {
            assetLedgerService.change(userId, AssetLedgerService.POINTS, order.getPointAmount(), 1, "mall_cancel", order.getId(), "订单取消退回");
        }
        order.setStatus(3);
        iMallOrderService.updateById(order);
    }

    @Override
    public IPage<MallOrderVo> listMyOrders(Long userId, MallOrderPageBo bo) {
        Page<MallOrder> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<MallOrder> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(MallOrder::getUserId, userId)
                .eq(bo.getStatus() != null, MallOrder::getStatus, bo.getStatus())
                .orderByDesc(MallOrder::getCreateTime);
        applyOrderTypeFilter(qw, bo.getOrderType());
        applyExchangeStatusFilter(qw, bo.getExchangeStatus());
        IPage<MallOrder> iPage = iMallOrderService.page(page, qw);
        return mapOrderPage(iPage);
    }

    private void applyOrderTypeFilter(QueryWrapper<MallOrder> qw, String orderType) {
        if (StringUtils.isBlank(orderType) || "all".equalsIgnoreCase(orderType)) {
            return;
        }
        if ("exchange".equalsIgnoreCase(orderType)) {
            qw.lambda().and(wrapper -> wrapper.eq(MallOrder::getBizType, 4).or().eq(MallOrder::getPayType, 2));
            return;
        }
        if ("product".equalsIgnoreCase(orderType)) {
            qw.lambda().and(wrapper ->
                    wrapper.and(w -> w.ne(MallOrder::getBizType, 4).or().isNull(MallOrder::getBizType))
                           .and(w -> w.ne(MallOrder::getPayType, 2).or().isNull(MallOrder::getPayType)));
        }
    }

    @Override
    public IPage<ExchangeRecordVo> listExchangeRecords(Long userId, MallOrderPageBo bo) {
        Page<MallOrder> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        Long resolvedUserId = userId;
        if (resolvedUserId == null) {
            List<Long> userIds = userIdResolveHelper.resolveOptional(bo.getUserId(), bo.getPhone(), bo.getUsername());
            if (userIds != null && userIds.isEmpty()) {
                return PageUtil.copyPage(page, Collections.emptyList());
            }
            if (userIds != null && userIds.size() == 1) {
                resolvedUserId = userIds.get(0);
            } else if (userIds != null) {
                QueryWrapper<MallOrder> multiQw = new QueryWrapper<>();
                multiQw.lambda()
                        .in(MallOrder::getUserId, userIds)
                        .and(wrapper -> wrapper.eq(MallOrder::getBizType, 4).or().eq(MallOrder::getPayType, 2))
                        .orderByDesc(MallOrder::getCreateTime);
                applyExchangeStatusFilter(multiQw, bo.getExchangeStatus());
                IPage<MallOrder> multiPage = iMallOrderService.page(page, multiQw);
                return mapExchangePage(multiPage);
            }
        }
        QueryWrapper<MallOrder> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(resolvedUserId != null, MallOrder::getUserId, resolvedUserId)
                .and(wrapper -> wrapper.eq(MallOrder::getBizType, 4).or().eq(MallOrder::getPayType, 2))
                .orderByDesc(MallOrder::getCreateTime);
        applyExchangeStatusFilter(qw, bo.getExchangeStatus());
        IPage<MallOrder> iPage = iMallOrderService.page(page, qw);
        return mapExchangePage(iPage);
    }

    private IPage<ExchangeRecordVo> mapExchangePage(IPage<MallOrder> iPage) {
        List<ExchangeRecordVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (MallOrder record : iPage.getRecords()) {
                ExchangeRecordVo vo = new ExchangeRecordVo();
                vo.setId(record.getId());
                vo.setOrderNo(record.getOrderNo());
                vo.setPoints(record.getPointAmount());
                vo.setDate(record.getCreateTime() == null ? "" :
                        record.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                MallSpu spu = record.getSpuId() == null ? null : iMallSpuService.getById(record.getSpuId());
                vo.setTitle(spu == null ? "周边兑换" : spu.getName());
                vo.setStatus(resolveExchangeStatus(record.getStatus()));
                vo.setQuantity(record.getQuantity());
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    private void applyExchangeStatusFilter(QueryWrapper<MallOrder> qw, String exchangeStatus) {
        if (exchangeStatus == null || exchangeStatus.isEmpty() || "all".equalsIgnoreCase(exchangeStatus)) {
            return;
        }
        if ("done".equalsIgnoreCase(exchangeStatus) || "已完成".equals(exchangeStatus)) {
            qw.lambda().eq(MallOrder::getStatus, 2);
            return;
        }
        if ("pending".equalsIgnoreCase(exchangeStatus) || "进行中".equals(exchangeStatus)) {
            qw.lambda().in(MallOrder::getStatus, 0, 1);
        }
    }

    private String resolveExchangeStatus(Integer status) {
        if (status == null) {
            return "进行中";
        }
        switch (status) {
            case 2: return "已完成";
            case 3: return "已取消";
            case 4: return "已退款";
            default: return "进行中";
        }
    }

    @Override
    public MallOrderVo info(Long userId, MallOrderInfoBo bo) {
        MallOrder order = iMallOrderService.getById(bo.getId());
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(PetError.ORDER_NOT_FOUND);
        }
        MallOrderVo vo = new MallOrderVo();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    @Override
    public IPage<MallOrderVo> adminPage(MallOrderPageBo bo) {
        Page<MallOrder> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        List<Long> userIds = userIdResolveHelper.resolveOptional(bo.getUserId(), bo.getPhone(), bo.getUsername());
        if (userIds != null && userIds.isEmpty()) {
            return PageUtil.copyPage(page, Collections.emptyList());
        }
        QueryWrapper<MallOrder> qw = new QueryWrapper<>();
        qw.lambda()
                .in(userIds != null, MallOrder::getUserId, userIds)
                .like(StringUtils.isNotBlank(bo.getOrderNo()), MallOrder::getOrderNo, bo.getOrderNo())
                .eq(bo.getStatus() != null, MallOrder::getStatus, bo.getStatus())
                .eq(bo.getPayStatus() != null, MallOrder::getPayStatus, bo.getPayStatus())
                .eq(bo.getBizType() != null, MallOrder::getBizType, bo.getBizType())
                .orderByDesc(MallOrder::getCreateTime);
        applyOrderTypeFilter(qw, bo.getOrderType());
        applyExchangeStatusFilter(qw, bo.getExchangeStatus());
        IPage<MallOrder> iPage = iMallOrderService.page(page, qw);
        return mapOrderPage(iPage);
    }

    private IPage<MallOrderVo> mapOrderPage(IPage<MallOrder> iPage) {
        List<MallOrderVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            List<Long> spuIds = new ArrayList<>();
            for (MallOrder record : iPage.getRecords()) {
                if (record.getSpuId() != null && !spuIds.contains(record.getSpuId())) {
                    spuIds.add(record.getSpuId());
                }
            }
            java.util.Map<Long, MallSpu> spuMap = new java.util.HashMap<>();
            if (!spuIds.isEmpty()) {
                List<MallSpu> spuList = iMallSpuService.listByIds(spuIds);
                for (MallSpu spu : spuList) {
                    spuMap.put(spu.getId(), spu);
                }
            }
            for (MallOrder record : iPage.getRecords()) {
                MallOrderVo vo = new MallOrderVo();
                BeanUtils.copyProperties(record, vo);
                MallSpu spu = record.getSpuId() == null ? null : spuMap.get(record.getSpuId());
                if (spu != null) {
                    vo.setSpuName(spu.getName());
                    vo.setCoverUrl(spu.getCoverUrl());
                }
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public MallOrderVo adminInfo(MallOrderInfoBo bo) {
        MallOrder order = iMallOrderService.getById(bo.getId());
        if (order == null) {
            throw new BizException(PetError.ORDER_NOT_FOUND);
        }
        MallOrderVo vo = new MallOrderVo();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminCancel(MallOrderInfoBo bo) {
        MallOrder order = iMallOrderService.getById(bo.getId());
        if (order == null) {
            throw new BizException(PetError.ORDER_NOT_FOUND);
        }
        MallOrderCancelBo cancelBo = new MallOrderCancelBo();
        cancelBo.setId(bo.getId());
        cancelOrder(order.getUserId(), cancelBo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminRefund(MallOrderRefundBo bo) {
        MallOrder order = iMallOrderService.getById(bo.getId());
        if (order == null) {
            throw new BizException(PetError.ORDER_NOT_FOUND);
        }
        if (!Objects.equals(order.getPayStatus(), 3)) {
            throw new BizException("仅已支付订单可退款");
        }
        order.setStatus(4);
        order.setPayStatus(6);
        iMallOrderService.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateExchangeStatus(MallOrderExchangeStatusBo bo) {
        MallOrder order = iMallOrderService.getById(bo.getId());
        if (order == null || !isExchangeOrder(order)) {
            throw new BizException(PetError.ORDER_NOT_FOUND);
        }
        order.setStatus(bo.getStatus());
        iMallOrderService.updateById(order);
    }

    private boolean isExchangeOrder(MallOrder order) {
        return Objects.equals(order.getBizType(), 4) || Objects.equals(order.getPayType(), 2);
    }
}
