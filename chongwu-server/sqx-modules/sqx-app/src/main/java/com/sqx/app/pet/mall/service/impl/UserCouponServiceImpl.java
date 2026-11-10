package com.sqx.app.pet.mall.service.impl;

import com.sqx.app.pet.common.enums.PetError;
import com.sqx.app.pet.mall.db.service.ICouponTemplateService;
import com.sqx.app.pet.mall.db.service.IUserCouponService;
import com.sqx.app.pet.mall.domain.bo.UserCouponGrantBatchBo;
import com.sqx.app.pet.mall.domain.bo.UserCouponGrantBo;
import com.sqx.app.pet.mall.domain.bo.UserCouponListBo;
import com.sqx.app.pet.mall.domain.bo.UserCouponUseBo;
import com.sqx.app.pet.mall.domain.entity.CouponTemplate;
import com.sqx.app.pet.mall.domain.entity.UserCoupon;
import com.sqx.app.pet.mall.domain.vo.UserCouponVo;
import com.sqx.app.pet.mall.service.UserCouponService;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserCouponServiceImpl implements UserCouponService {

    @Resource
    private IUserCouponService iUserCouponService;
    @Resource
    private ICouponTemplateService iCouponTemplateService;

    @Override
    public List<UserCouponVo> myCoupons(Long userId) {
        return myCoupons(userId, null);
    }

    @Override
    public List<UserCouponVo> myCoupons(Long userId, UserCouponListBo bo) {
        List<UserCoupon> coupons = iUserCouponService.lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getCreateTime)
                .list();
        return coupons.stream()
                .map(this::toVo)
                .filter(vo -> matchStatusFilter(vo, bo == null ? null : bo.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantCoupon(UserCouponGrantBo bo) {
        CouponTemplate template = iCouponTemplateService.getById(bo.getTemplateId());
        if (template == null || !Objects.equals(template.getStatus(), 1)) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        int count = bo.getCount() == null || bo.getCount() < 1 ? 1 : bo.getCount();
        LocalDateTime now = LocalDateTime.now();
        Integer validDays = template.getValidDays() == null ? 30 : template.getValidDays();
        LocalDateTime expireTime = now.plusDays(validDays);
        for (int i = 0; i < count; i++) {
            UserCoupon coupon = new UserCoupon();
            coupon.setUserId(bo.getUserId());
            coupon.setTemplateId(template.getId());
            coupon.setStatus(1);
            coupon.setExpireTime(expireTime);
            iUserCouponService.save(coupon);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantCouponBatch(UserCouponGrantBatchBo bo) {
        if (bo.getUserIds() == null || bo.getUserIds().isEmpty()) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        for (Long userId : bo.getUserIds()) {
            UserCouponGrantBo single = new UserCouponGrantBo();
            single.setUserId(userId);
            single.setTemplateId(bo.getTemplateId());
            single.setCount(bo.getCount());
            grantCoupon(single);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(Long userId, UserCouponUseBo bo) {
        UserCoupon coupon = iUserCouponService.getById(bo.getCouponId());
        if (coupon == null || !Objects.equals(coupon.getUserId(), userId) || !Objects.equals(coupon.getStatus(), 1)) {
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        if (coupon.getExpireTime() != null && coupon.getExpireTime().isBefore(LocalDateTime.now())) {
            coupon.setStatus(3);
            iUserCouponService.updateById(coupon);
            throw new BizException(PetError.INFO_NOT_FOUND);
        }
        coupon.setStatus(2);
        coupon.setOrderId(bo.getOrderId());
        iUserCouponService.updateById(coupon);
    }

    private UserCouponVo toVo(UserCoupon coupon) {
        normalizeCouponStatus(coupon);
        UserCouponVo vo = new UserCouponVo();
        BeanUtils.copyProperties(coupon, vo);
        CouponTemplate template = coupon.getTemplateId() == null ? null :
                iCouponTemplateService.getById(coupon.getTemplateId());
        if (template != null) {
            vo.setTitle(template.getTitle());
            vo.setAmount(template.getAmountDesc());
            vo.setType(resolveCouponTypeText(template.getCouponType()));
            vo.setDesc(buildCouponDesc(template));
        }
        if (coupon.getExpireTime() != null) {
            vo.setExpire("有效期至 " + coupon.getExpireTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        vo.setStatusText(resolveStatusText(coupon.getStatus()));
        return vo;
    }

    private void normalizeCouponStatus(UserCoupon coupon) {
        if (coupon == null || !Objects.equals(coupon.getStatus(), 1)) {
            return;
        }
        if (coupon.getExpireTime() != null && coupon.getExpireTime().isBefore(LocalDateTime.now())) {
            coupon.setStatus(3);
            iUserCouponService.updateById(coupon);
        }
    }

    private boolean matchStatusFilter(UserCouponVo vo, String status) {
        if (StringUtils.isBlank(status)) {
            return true;
        }
        String text = vo.getStatusText();
        switch (status) {
            case "available":
                return "可用".equals(text);
            case "used":
                return "已用".equals(text);
            case "expired":
                return "已过".equals(text) || "已过期".equals(text);
            default:
                return text != null && text.contains(status);
        }
    }

    private String resolveStatusText(Integer status) {
        if (status == null) {
            return "可用";
        }
        switch (status) {
            case 2: return "已用";
            case 3: return "已过";
            default: return "可用";
        }
    }

    private String resolveCouponTypeText(String couponType) {
        if ("feed".equalsIgnoreCase(couponType)) {
            return "投喂优惠";
        }
        if ("store".equalsIgnoreCase(couponType)) {
            return "门店优惠";
        }
        return "积分兑换";
    }

    private String buildCouponDesc(CouponTemplate template) {
        if (template.getMinAmount() != null && template.getMinAmount().doubleValue() > 0) {
            return "满 " + template.getMinAmount().stripTrailingZeros().toPlainString() + " 可用";
        }
        return "全场通用";
    }
}
