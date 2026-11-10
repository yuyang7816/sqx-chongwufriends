package com.sqx.app.pet.payment.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.app.pet.payment.domain.bo.PetPayCreateBo;
import com.sqx.app.pet.payment.domain.bo.PetPayQueryBo;
import com.sqx.app.pet.payment.domain.vo.PetPayChannelVo;
import com.sqx.app.pet.payment.domain.vo.PetPayOrderVo;
import com.sqx.app.pet.payment.service.PetPayService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.pay.service.IPayService;
import com.sqx.common.shiro.utils.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Tag(name = "App-Pet Pay", description = "宠物支付")
@RestController
@RequestMapping("/app/pet/payment")
public class AppPetPayController {

    @Resource
    private PetPayService petPayService;
    @Resource
    private IPayService iPayService;

    @Operation(summary = "可用支付渠道")
    @PostMapping("channels")
    public Result<PetPayChannelVo> channels() {
        return Result.success(petPayService.getChannels());
    }

    @Login
    @Idempotent
    @Operation(summary = "创建支付单")
    @PostMapping("create")
    public Result<PetPayOrderVo> create(@RequestBody @Validated PetPayCreateBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(petPayService.createPayOrder(userId, bo));
    }

    @Login
    @Operation(summary = "查询支付状态")
    @PostMapping("query")
    public Result<PetPayOrderVo> query(@RequestBody @Validated PetPayQueryBo bo) {
        Long userId = UserContextHolder.getAppUserId();
        return Result.success(petPayService.queryStatus(userId, bo));
    }

    @Operation(hidden = true)
    @PostMapping("notify/{channel}")
    public String notify(HttpServletRequest request, @PathVariable String channel) {
        return iPayService.notify(request, channel);
    }

    @Operation(summary = "支付宝 WAP 支付页（微信小程序内嵌 web-view）")
    @GetMapping("alipay/wap/{payNo}")
    public void alipayWap(@PathVariable String payNo, HttpServletResponse response) throws IOException {
        String html = petPayService.getAlipayWapHtml(payNo);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }
}
