package com.sqx.app.pet.user.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.app.pet.user.domain.bo.CancelApplyBo;
import com.sqx.app.pet.user.domain.bo.NotificationConfigBo;
import com.sqx.app.pet.user.domain.bo.PetUserUpdateBo;
import com.sqx.app.pet.user.domain.vo.NotificationConfigVo;
import com.sqx.app.pet.user.domain.vo.WecomConfigVo;
import com.sqx.app.pet.user.domain.vo.UserCenterSummaryVo;
import com.sqx.app.pet.user.service.PetUserService;
import com.sqx.app.pet.user.service.UserCenterService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.shiro.utils.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "App-Pet User", description = "宠物用户")
@RestController
@RequestMapping("/app/pet/user")
public class AppPetUserController {

    @Resource
    private PetUserService petUserService;
    @Resource
    private UserCenterService userCenterService;

    @Login
    @Operation(summary = "通知配置查询")
    @GetMapping("notification")
    public Result<NotificationConfigVo> notificationGet() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(petUserService.getNotification(userId));
    }

    @Login
    @Operation(summary = "通知配置查询POST")
    @PostMapping("notification")
    public Result<NotificationConfigVo> notificationPost() {
        return notificationGet();
    }

    @Login
    @Idempotent
    @Operation(summary = "保存通知配置")
    @PostMapping("notification/save")
    public Result<Void> notificationSave(@RequestBody NotificationConfigBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        petUserService.saveNotification(userId, bo);
        return Result.success();
    }

    @Operation(summary = "企微配置")
    @GetMapping("wecom")
    public Result<WecomConfigVo> wecomGet() {
        return Result.success(petUserService.getWecom());
    }

    @Operation(summary = "企微配置POST")
    @PostMapping("wecom")
    public Result<WecomConfigVo> wecomPost() {
        return wecomGet();
    }

    @Login
    @Operation(summary = "个人中心汇总")
    @PostMapping("center/summary")
    public Result<UserCenterSummaryVo> centerSummary() {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(userCenterService.getSummary(userId));
    }

    @Login
    @Idempotent
    @Operation(summary = "注销申请")
    @PostMapping("cancelApply")
    public Result<Void> cancelApply(@RequestBody CancelApplyBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        petUserService.cancelApply(userId, bo);
        return Result.success();
    }

    @Login
    @Idempotent
    @Operation(summary = "更新资料(含昵称敏感词)")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated PetUserUpdateBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        petUserService.updateProfile(userId, bo);
        return Result.success();
    }
}
