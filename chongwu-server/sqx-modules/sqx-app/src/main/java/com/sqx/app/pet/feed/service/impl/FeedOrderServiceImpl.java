package com.sqx.app.pet.feed.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.support.UserIdResolveHelper;
import com.sqx.app.pet.archive.db.service.IPetArchiveService;
import com.sqx.app.pet.archive.domain.entity.PetArchive;
import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.feed.db.service.IFeedFeedbackService;
import com.sqx.app.pet.feed.db.service.IFeedOrderMessageService;
import com.sqx.app.pet.feed.db.service.IFeedOrderService;
import com.sqx.app.pet.feed.domain.bo.*;
import com.sqx.app.pet.feed.domain.entity.FeedFeedback;
import com.sqx.app.pet.feed.domain.entity.FeedOrder;
import com.sqx.app.pet.feed.domain.entity.FeedOrderMessage;
import com.sqx.app.pet.feed.domain.vo.FeedGiftSummaryVo;
import com.sqx.app.pet.feed.domain.vo.FeedOrderVo;
import com.sqx.app.pet.feed.service.FeedOrderService;
import com.sqx.app.pet.mall.db.service.IMallSpuService;
import com.sqx.app.pet.mall.domain.entity.MallSpu;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FeedOrderServiceImpl implements FeedOrderService {

    @Resource
    private IFeedOrderService iFeedOrderService;
    @Resource
    private IFeedOrderMessageService iFeedOrderMessageService;
    @Resource
    private IFeedFeedbackService iFeedFeedbackService;
    @Resource
    private IPetArchiveService iPetArchiveService;
    @Resource
    private IMallSpuService iMallSpuService;
    @Resource
    private UserIdResolveHelper userIdResolveHelper;

    private static final int FEED_BIZ_TYPE = 2;

    @Override
    public void saveInfo(FeedOrderSaveBo bo) {
        FeedOrder entity = new FeedOrder();
        BeanUtils.copyProperties(bo, entity);
        iFeedOrderService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedOrderVo createForUser(Long userId, FeedOrderCreateBo bo) {
        String productName = bo.getProductName();
        BigDecimal amount = bo.getAmount();
        int quantity = bo.getQuantity() == null ? 1 : bo.getQuantity();

        if (CollectionUtils.isNotEmpty(bo.getItems())) {
            List<String> nameParts = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalQty = 0;
            for (FeedOrderItemBo item : bo.getItems()) {
                if (item == null || item.getProductId() == null) {
                    throw new BizException("商品ID不能为空");
                }
                int qty = item.getQuantity() == null ? 0 : item.getQuantity();
                if (qty < 1) {
                    throw new BizException("商品数量至少为1");
                }
                MallSpu spu = iMallSpuService.getById(item.getProductId());
                if (spu == null) {
                    throw new BizException(PetError.INFO_NOT_FOUND);
                }
                if (!Objects.equals(spu.getStatus(), 1) || !Objects.equals(spu.getBizType(), FEED_BIZ_TYPE)) {
                    throw new BizException("商品未上架或不是投喂商品: " + spu.getName());
                }
                BigDecimal price = spu.getPrice() == null ? BigDecimal.ZERO : spu.getPrice();
                totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(qty)));
                totalQty += qty;
                nameParts.add(spu.getName() + "×" + qty);
            }
            productName = String.join("、", nameParts);
            amount = totalAmount.setScale(2, RoundingMode.HALF_UP);
            quantity = totalQty;
        } else if (StringUtils.isBlank(productName)) {
            throw new BizException("商品名称不能为空");
        }

        FeedOrder order = new FeedOrder();
        order.setOrderNo(GenUtil.genOrderNo());
        order.setUserId(userId);
        order.setPetId(bo.getPetId());
        order.setProductName(productName);
        order.setAmount(amount);
        order.setQuantity(quantity);
        order.setMessage(bo.getMessage());
        order.setStatus(1);
        iFeedOrderService.save(order);
        if (StringUtils.isNotBlank(bo.getMessage()) || StringUtils.isNotBlank(bo.getSignature())) {
            FeedOrderMessage msg = new FeedOrderMessage();
            msg.setOrderId(order.getId());
            msg.setMessage(bo.getMessage());
            msg.setSignature(bo.getSignature());
            iFeedOrderMessageService.save(msg);
        }
        FeedFeedback feedback = new FeedFeedback();
        feedback.setOrderId(order.getId());
        feedback.setVideoUrl("pending");
        feedback.setFeedbackStatus(1);
        iFeedFeedbackService.save(feedback);
        FeedOrderVo vo = new FeedOrderVo();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    @Override
    public IPage<FeedOrderVo> listMyGifts(Long userId, FeedOrderPageBo bo) {
        Page<FeedOrder> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<FeedOrder> qw = new QueryWrapper<>();
        qw.lambda().eq(FeedOrder::getUserId, userId).orderByDesc(FeedOrder::getCreateTime);
        IPage<FeedOrder> iPage = iFeedOrderService.page(page, qw);
        List<FeedOrderVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (FeedOrder record : iPage.getRecords()) {
                FeedOrderVo vo = new FeedOrderVo();
                BeanUtils.copyProperties(record, vo);
                enrichGiftVo(vo, record);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFeedback(Long userId, FeedFeedbackUploadBo bo) {
        FeedOrder order = iFeedOrderService.getById(bo.getOrderId());
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(PetError.ORDER_NOT_FOUND);
        }
        FeedFeedback feedback = iFeedFeedbackService.lambdaQuery()
                .eq(FeedFeedback::getOrderId, bo.getOrderId())
                .one();
        if (feedback == null) {
            feedback = new FeedFeedback();
            feedback.setOrderId(bo.getOrderId());
        }
        feedback.setVideoUrl(bo.getVideoUrl());
        feedback.setFeedbackStatus(2);
        iFeedFeedbackService.saveOrUpdate(feedback);
        order.setStatus(3);
        iFeedOrderService.updateById(order);
    }

    @Override
    public FeedGiftSummaryVo giftSummary(Long userId) {
        List<FeedOrder> orders = iFeedOrderService.lambdaQuery()
                .eq(FeedOrder::getUserId, userId)
                .orderByDesc(FeedOrder::getCreateTime)
                .list();
        FeedGiftSummaryVo summary = new FeedGiftSummaryVo();
        summary.setTotalCount(orders.size());
        int feedbackCount = 0;
        int pendingCount = 0;
        for (FeedOrder order : orders) {
            if (Objects.equals(order.getStatus(), 3)) {
                feedbackCount++;
            } else {
                pendingCount++;
            }
        }
        summary.setFeedbackCount(feedbackCount);
        summary.setPendingCount(pendingCount);
        if (!orders.isEmpty() && orders.get(0).getCreateTime() != null) {
            long days = ChronoUnit.DAYS.between(orders.get(0).getCreateTime().toLocalDate(), LocalDateTime.now().toLocalDate());
            summary.setLatestGiftText(days <= 0 ? "今天" : days + " 天前");
        } else {
            summary.setLatestGiftText("--");
        }
        return summary;
    }

    @Override
    public IPage<FeedOrderVo> pageInfo(FeedOrderPageBo bo) {
        Page<FeedOrder> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        List<Long> userIds = userIdResolveHelper.resolveOptional(bo.getUserId(), bo.getPhone(), bo.getUsername());
        if (userIds != null && userIds.isEmpty()) {
            return PageUtil.copyPage(page, Collections.emptyList());
        }
        QueryWrapper<FeedOrder> qw = new QueryWrapper<>();
        qw.lambda()
                .in(userIds != null, FeedOrder::getUserId, userIds)
                .like(StringUtils.isNotBlank(bo.getOrderNo()), FeedOrder::getOrderNo, bo.getOrderNo())
                .eq(bo.getStatus() != null, FeedOrder::getStatus, bo.getStatus())
                .orderByDesc(FeedOrder::getCreateTime);
        IPage<FeedOrder> iPage = iFeedOrderService.page(page, qw);
        List<FeedOrderVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (FeedOrder record : iPage.getRecords()) {
                FeedOrderVo vo = new FeedOrderVo();
                BeanUtils.copyProperties(record, vo);
                enrichGiftVo(vo, record);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public FeedOrderVo info(FeedOrderInfoBo bo) {
        FeedOrder entity = iFeedOrderService.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        FeedOrderVo vo = new FeedOrderVo();
        BeanUtils.copyProperties(entity, vo);
        enrichGiftVo(vo, entity);
        attachVideoUrl(vo, entity.getId());
        return vo;
    }

    @Override
    public void updateInfo(FeedOrderUpdateBo bo) {
        FeedOrder entity = new FeedOrder();
        BeanUtils.copyProperties(bo, entity);
        iFeedOrderService.updateById(entity);
    }

    @Override
    public void delete(FeedOrderInfoBo bo) {
        iFeedOrderService.removeById(bo.getId());
    }

    @Override
    public List<FeedOrderVo> list() {
        List<FeedOrder> list = iFeedOrderService.list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            FeedOrderVo vo = new FeedOrderVo();
            BeanUtils.copyProperties(e, vo);
            enrichGiftVo(vo, e);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUploadFeedback(FeedFeedbackUploadBo bo) {
        FeedOrder order = iFeedOrderService.getById(bo.getOrderId());
        if (order == null) {
            throw new BizException(PetError.ORDER_NOT_FOUND);
        }
        FeedFeedback feedback = iFeedFeedbackService.lambdaQuery()
                .eq(FeedFeedback::getOrderId, bo.getOrderId())
                .one();
        if (feedback == null) {
            feedback = new FeedFeedback();
            feedback.setOrderId(bo.getOrderId());
        }
        feedback.setVideoUrl(bo.getVideoUrl());
        feedback.setFeedbackStatus(2);
        iFeedFeedbackService.saveOrUpdate(feedback);
        order.setStatus(3);
        iFeedOrderService.updateById(order);
    }

    private void attachVideoUrl(FeedOrderVo vo, Long orderId) {
        if (orderId == null) {
            return;
        }
        FeedFeedback feedback = iFeedFeedbackService.lambdaQuery()
                .eq(FeedFeedback::getOrderId, orderId)
                .one();
        if (feedback != null && StringUtils.isNotBlank(feedback.getVideoUrl()) && !"pending".equals(feedback.getVideoUrl())) {
            vo.setVideoUrl(feedback.getVideoUrl());
        }
    }

    private void enrichGiftVo(FeedOrderVo vo, FeedOrder order) {
        attachVideoUrl(vo, order.getId());
        if (order.getCreateTime() != null) {
            vo.setDate(order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        vo.setItem(order.getProductName());
        vo.setNote(order.getMessage());
        if (order.getPetId() != null) {
            PetArchive pet = iPetArchiveService.getById(order.getPetId());
            if (pet != null) {
                vo.setPet(pet.getName());
            }
        }
        vo.setStatusText(resolveGiftStatus(order.getStatus()));
    }

    private String resolveGiftStatus(Integer status) {
        if (status == null) {
            return "待反馈";
        }
        switch (status) {
            case 3: return "已反馈";
            case 2: return "预计 3 天内反馈";
            default: return "待反馈";
        }
    }
}
