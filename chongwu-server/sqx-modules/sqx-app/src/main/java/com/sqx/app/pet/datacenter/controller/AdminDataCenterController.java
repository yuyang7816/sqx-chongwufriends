package com.sqx.app.pet.datacenter.controller;

import com.sqx.app.pet.datacenter.domain.vo.DataCenterOverviewVo;
import com.sqx.app.pet.datacenter.service.DataCenterService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "Admin-数据中心", description = "门店经营概览")
@RestController
@RequestMapping("/admin/pet/datacenter")
public class AdminDataCenterController {

    @Resource
    private DataCenterService dataCenterService;

    @Operation(summary = "经营概览（今日指标/待办/近7日趋势）")
    @PostMapping("overview")
    public Result<DataCenterOverviewVo> overview() {
        return Result.success(dataCenterService.overview());
    }
}
