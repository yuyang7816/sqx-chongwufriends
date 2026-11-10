package com.sqx.app.pet.adopt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.adopt.db.service.IAdoptVisitService;
import com.sqx.app.pet.adopt.domain.bo.*;
import com.sqx.app.pet.adopt.domain.entity.AdoptVisit;
import com.sqx.app.pet.adopt.domain.vo.AdoptApplicationVo;
import com.sqx.app.pet.adopt.service.AdoptApplicationService;
import com.sqx.app.pet.common.annotation.Idempotent;
import com.sqx.common.core.domain.Result;
import com.sqx.common.mybatis.domain.PageParam;
import com.sqx.common.mybatis.utils.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "Admin-Adopt Application", description = "Admin-Adopt Application")
@RestController
@RequestMapping("/admin/pet/adopt")
public class AdminAdoptApplicationController {

    @Resource
    private AdoptApplicationService adoptApplication;
    @Resource
    private IAdoptVisitService iAdoptVisitService;

    @Operation(summary = "save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated AdoptApplicationSaveBo bo) {
        adoptApplication.saveInfo(bo);
        return Result.success();
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AdoptApplicationVo.class)))
    @PostMapping("page")
    public Result<IPage<AdoptApplicationVo>> page(@RequestBody @Validated AdoptApplicationPageBo bo) {
        return Result.success(adoptApplication.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AdoptApplicationVo.class)))
    @PostMapping("info")
    public Result<AdoptApplicationVo> info(@RequestBody @Validated AdoptApplicationInfoBo bo) {
        return Result.success(adoptApplication.info(bo));
    }

    @Operation(summary = "update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated AdoptApplicationUpdateBo bo) {
        adoptApplication.updateInfo(bo);
        return Result.success();
    }

    @Operation(summary = "delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated AdoptApplicationInfoBo bo) {
        adoptApplication.delete(bo);
        return Result.success();
    }

    @Operation(summary = "回访记录分页")
    @PostMapping("visit/page")
    public Result<IPage<AdoptVisit>> visitPage(@RequestBody VisitPageBo bo) {
        Page<AdoptVisit> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<AdoptVisit> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(bo.getApplicationId() != null, AdoptVisit::getApplicationId, bo.getApplicationId())
                .orderByDesc(AdoptVisit::getCreateTime);
        return Result.success(iAdoptVisitService.page(page, qw));
    }

    @Idempotent
    @Operation(summary = "新增回访")
    @PostMapping("visit/save")
    public Result<Void> visitSave(@RequestBody VisitSaveBo bo) {
        AdoptVisit visit = new AdoptVisit();
        visit.setApplicationId(bo.getApplicationId());
        visit.setVisitTime(bo.getVisitTime());
        visit.setContent(bo.getContent());
        visit.setOperatorId(bo.getOperatorId());
        iAdoptVisitService.save(visit);
        return Result.success();
    }

    public static class VisitPageBo extends PageParam {
        private Long applicationId;

        public Long getApplicationId() {
            return applicationId;
        }

        public void setApplicationId(Long applicationId) {
            this.applicationId = applicationId;
        }
    }

    public static class VisitSaveBo {
        private Long applicationId;
        private java.time.LocalDateTime visitTime;
        private String content;
        private Long operatorId;

        public Long getApplicationId() {
            return applicationId;
        }

        public void setApplicationId(Long applicationId) {
            this.applicationId = applicationId;
        }

        public java.time.LocalDateTime getVisitTime() {
            return visitTime;
        }

        public void setVisitTime(java.time.LocalDateTime visitTime) {
            this.visitTime = visitTime;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Long getOperatorId() {
            return operatorId;
        }

        public void setOperatorId(Long operatorId) {
            this.operatorId = operatorId;
        }
    }
}
