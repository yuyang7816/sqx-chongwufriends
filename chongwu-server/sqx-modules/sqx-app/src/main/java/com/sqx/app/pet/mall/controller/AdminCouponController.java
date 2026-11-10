package com.sqx.app.pet.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.support.UserIdResolveHelper;
import com.sqx.app.pet.mall.db.service.ICouponTemplateService;
import com.sqx.app.pet.mall.db.service.IUserCouponService;
import com.sqx.app.pet.mall.domain.bo.UserCouponGrantBatchBo;
import com.sqx.app.pet.mall.domain.bo.UserCouponGrantBo;
import com.sqx.app.pet.mall.domain.entity.CouponTemplate;
import com.sqx.app.pet.mall.domain.entity.UserCoupon;
import com.sqx.app.pet.mall.service.UserCouponService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.mybatis.domain.PageParam;
import com.sqx.common.mybatis.utils.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Tag(name = "Admin-Coupon", description = "优惠券模板管理")
@RestController
@RequestMapping("/admin/pet/coupon")
public class AdminCouponController {

    @Resource
    private ICouponTemplateService iCouponTemplateService;
    @Resource
    private UserCouponService userCouponService;
    @Resource
    private IUserCouponService iUserCouponService;
    @Resource
    private UserIdResolveHelper userIdResolveHelper;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class CouponPageBo extends PageParam {
        private String title;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UserCouponPageBo extends PageParam {
        private Long userId;
        private String phone;
        private String username;
        private Integer status;
    }

    @Operation(summary = "券模板分页")
    @PostMapping("page")
    public Result<IPage<CouponTemplate>> page(@RequestBody CouponPageBo bo) {
        Page<CouponTemplate> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<CouponTemplate> qw = new QueryWrapper<>();
        qw.lambda()
                .like(bo.getTitle() != null, CouponTemplate::getTitle, bo.getTitle())
                .orderByDesc(CouponTemplate::getCreateTime);
        return Result.success(iCouponTemplateService.page(page, qw));
    }

    @Operation(summary = "用户券包分页")
    @PostMapping("user/page")
    public Result<IPage<UserCoupon>> userCouponPage(@RequestBody UserCouponPageBo bo) {
        Page<UserCoupon> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        List<Long> userIds = userIdResolveHelper.resolveOptional(bo.getUserId(), bo.getPhone(), bo.getUsername());
        if (userIds != null && userIds.isEmpty()) {
            page.setRecords(Collections.emptyList());
            page.setTotal(0);
            return Result.success(page);
        }
        QueryWrapper<UserCoupon> qw = new QueryWrapper<>();
        qw.lambda()
                .in(userIds != null, UserCoupon::getUserId, userIds)
                .eq(bo.getStatus() != null, UserCoupon::getStatus, bo.getStatus())
                .orderByDesc(UserCoupon::getCreateTime);
        return Result.success(iUserCouponService.page(page, qw));
    }

    @Operation(summary = "保存券模板")
    @PostMapping("save")
    public Result<Void> save(@RequestBody CouponTemplate template) {
        iCouponTemplateService.save(template);
        return Result.success();
    }

    @Operation(summary = "更新券模板")
    @PostMapping("update")
    public Result<Void> update(@RequestBody CouponTemplate template) {
        iCouponTemplateService.updateById(template);
        return Result.success();
    }

    @Operation(summary = "删除券模板")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody CouponTemplate template) {
        iCouponTemplateService.removeById(template.getId());
        return Result.success();
    }

    @Operation(summary = "向用户发券")
    @PostMapping("grant")
    public Result<Void> grant(@RequestBody @Validated UserCouponGrantBo bo) {
        userCouponService.grantCoupon(bo);
        return Result.success();
    }

    @Operation(summary = "批量向用户发券")
    @PostMapping("grant/batch")
    public Result<Void> grantBatch(@RequestBody @Validated UserCouponGrantBatchBo bo) {
        userCouponService.grantCouponBatch(bo);
        return Result.success();
    }
}
