package com.sqx.app.referral.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.referral.commission.db.service.IReferralCommissionService;
import com.sqx.app.referral.commission.domain.entity.ReferralCommission;
import com.sqx.app.referral.commission.domain.vo.ReferralListVo;
import com.sqx.app.referral.user.db.service.IReferralUserCommissionService;
import com.sqx.app.referral.user.domain.bo.ReferralUserCommissionInfoBo;
import com.sqx.app.referral.user.domain.bo.ReferralUserCommissionUpdateBo;
import com.sqx.app.referral.user.domain.entity.ReferralUserCommission;
import com.sqx.app.referral.user.domain.vo.ReferralDataVo;
import com.sqx.app.referral.user.domain.vo.ReferralLawyerVo;
import com.sqx.app.referral.user.domain.vo.ReferralUserCommissionVo;
import com.sqx.app.referral.user.service.ReferralUserCommissionService;
import com.sqx.app.utils.DateUtil;
import com.sqx.app.utils.model.StartAndEndTime;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.shiro.utils.UserContextHolder;
import com.sqx.system.db.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 用户佣金 服务实现类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:53
 */
@Slf4j
@Service
public class ReferralUserCommissionServiceImpl implements ReferralUserCommissionService {

    @Resource
    private IReferralUserCommissionService iReferralUserCommissionService;
    @Resource
    private ISysConfigService iSysConfigService;
    @Resource
    private IUserService iUserService;
    @Resource
    private IReferralCommissionService iReferralCommissionService;

    @Override
    public ReferralUserCommissionVo info(Long userId) {
        ReferralUserCommission referralUserCommission = iReferralUserCommissionService.getOne(new LambdaQueryWrapper<ReferralUserCommission>().eq(ReferralUserCommission::getUserId, userId));
        if (Objects.isNull(referralUserCommission)) {
            throw new BizException("信息不存在");
        }

        ReferralUserCommissionVo vo = new ReferralUserCommissionVo();
        BeanUtils.copyProperties(referralUserCommission, vo);
        return vo;
    }

    @Override
    public void updateInfo(ReferralUserCommissionUpdateBo referralUserCommissionUpdateBo) {
        ReferralUserCommission referralUserCommission = new ReferralUserCommission();
        BeanUtils.copyProperties(referralUserCommissionUpdateBo, referralUserCommission);

        iReferralUserCommissionService.updateById(referralUserCommission);
    }

    @Override
    public void delete(ReferralUserCommissionInfoBo referralUserCommissionInfoBo) {
        iReferralUserCommissionService.removeById(referralUserCommissionInfoBo.getId());
    }

    @Override
    public void saveUserCommission(Long orderId) {
//        OmsOrder omsOrder = iOmsOrderService.getById(orderId);
//        if (Objects.isNull(omsOrder)) {
//            log.info("订单信息不存在, 不计算佣金");
//            return;
//        }
//
//        UserEntity userInfo = iUserService.getById(omsOrder.getUserId());
//        if (Objects.isNull(userInfo)) {
//            log.error("用户信息不存在，取消计算佣金");
//            return;
//        }
//
//        UserEntity inviteUserInfo = iUserService.getById(userInfo.getInviterId());
//        if (Objects.isNull(inviteUserInfo)) {
//            log.info("邀请用户不存在，不计算佣金");
//            return;
//        }
//
//        if (Objects.equals(2, inviteUserInfo.getStatus())) {
//            log.info("邀请用户被禁用，取消计算佣金");
//            return;
//        }
//
//        // 佣金比例
//        String commissionRateStr = iSysConfigService.getConfigValue("biz_referral_commission_rate");
//        BigDecimal commissionRate = new BigDecimal(commissionRateStr);
//
//        // 佣金
//        BigDecimal payAmount = omsOrder.getPayAmount();
//        BigDecimal commission = payAmount.multiply(commissionRate);
//
//        // 增加用户佣金
//        iReferralUserCommissionService.incr(inviteUserInfo.getUserId(), commission);
//
//        // 保存用户佣金信息
//        ReferralCommission referralCommission = new ReferralCommission();
//        referralCommission.setUserId(inviteUserInfo.getUserId());
//        referralCommission.setOrderId(orderId);
//        referralCommission.setRate(commissionRate);
//        referralCommission.setAmount(commission);
//        referralCommission.setType(1);
//        iReferralCommissionService.save(referralCommission);
    }

    @Override
    public ReferralDataVo referralData(Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (Objects.isNull(user)) {
            throw new BizException("用户不存在");
        }

        // 邀请人数
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getSuperiorInvitationCode, user.getInvitationCode());
        long count = iUserService.count(queryWrapper);

        // 佣金
        BigDecimal commission = iReferralCommissionService.sumUserCommission(userId);

        ReferralDataVo vo = new ReferralDataVo();
        vo.setNum(count);
        vo.setCommission(commission);
        return vo;
    }

    @Override
    public IPage<UserEntity> referralUserList(Long userId) {
        Page<UserEntity> page = new Page<>(1, 10);

        UserEntity user = iUserService.getById(userId);
        if (Objects.isNull(user)) {
            throw new BizException("用户不存在");
        }

        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getSuperiorInvitationCode, user.getInvitationCode());
        IPage<UserEntity> userEntityPage = iUserService.page(page, queryWrapper);

        return userEntityPage;
    }

    @Override
    public IPage<ReferralListVo> referralList(Long userId) {
        Page<ReferralCommission> page = new Page<>(1, 10);

        IPage<ReferralListVo> voIPage = iReferralCommissionService.referralList(page, userId);

        return voIPage;
    }

    @Override
    public ReferralLawyerVo lawyer() {
        ReferralCommission referralCommission = iReferralCommissionService.getByUserId(UserContextHolder.getAppUserId());
        if (Objects.isNull(referralCommission)) {
            log.error("用户[{}]资产不存在", UserContextHolder.getAppUserId());
            throw new BizException("用户资产不存在");
        }

        // 昨日收益
        StartAndEndTime startAndEndTime = DateUtil.getStartAndEndTime(LocalDate.now().plusDays(1));
        BigDecimal yesterdayEarn = iReferralCommissionService.sumAmount(UserContextHolder.getAppUserId(), startAndEndTime.getStartTime(), startAndEndTime.getEndTime());

        ReferralLawyerVo vo = new ReferralLawyerVo();
        vo.setAmount(referralCommission.getAmount());
        vo.setYesterdayEarn(yesterdayEarn);
        return vo;
    }
}
