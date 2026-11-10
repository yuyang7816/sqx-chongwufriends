package com.sqx.app.pet.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.community.db.service.ICommunityComplaintTypeService;
import com.sqx.app.pet.community.db.service.ICommunityReportService;
import com.sqx.app.pet.community.domain.entity.CommunityComplaintType;
import com.sqx.app.pet.community.domain.entity.CommunityReport;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.domain.PageParam;
import com.sqx.common.mybatis.utils.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "Admin-Community Complaint Type", description = "朋友圈投诉类型管理")
@RestController
@RequestMapping("/admin/pet/community/complaint-type")
public class AdminCommunityComplaintTypeController {

    @Resource
    private ICommunityComplaintTypeService iCommunityComplaintTypeService;
    @Resource
    private ICommunityReportService iCommunityReportService;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ComplaintTypePageBo extends PageParam {
        private String name;
        private Integer status;
    }

    @Operation(summary = "投诉类型分页")
    @PostMapping("page")
    public Result<IPage<CommunityComplaintType>> page(@RequestBody ComplaintTypePageBo bo) {
        Page<CommunityComplaintType> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<CommunityComplaintType> qw = new QueryWrapper<>();
        qw.lambda()
                .like(StringUtils.isNotBlank(bo.getName()), CommunityComplaintType::getName, bo.getName())
                .eq(bo.getStatus() != null, CommunityComplaintType::getStatus, bo.getStatus())
                .orderByAsc(CommunityComplaintType::getSort)
                .orderByDesc(CommunityComplaintType::getCreateTime);
        return Result.success(iCommunityComplaintTypeService.page(page, qw));
    }

    @Operation(summary = "投诉类型列表")
    @PostMapping("list")
    public Result<List<CommunityComplaintType>> list() {
        QueryWrapper<CommunityComplaintType> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(CommunityComplaintType::getStatus, 1)
                .orderByAsc(CommunityComplaintType::getSort);
        return Result.success(iCommunityComplaintTypeService.list(qw));
    }

    @Operation(summary = "保存投诉类型")
    @PostMapping("save")
    public Result<Void> save(@RequestBody CommunityComplaintType entity) {
        String trimmed = validateName(entity.getName(), null);
        entity.setName(trimmed);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        iCommunityComplaintTypeService.saveType(entity);
        return Result.success();
    }

    @Operation(summary = "更新投诉类型")
    @PostMapping("update")
    public Result<Void> update(@RequestBody CommunityComplaintType entity) {
        if (entity.getId() == null) {
            throw new BizException("投诉类型ID不能为空");
        }
        String trimmed = validateName(entity.getName(), entity.getId());
        entity.setName(trimmed);
        iCommunityComplaintTypeService.updateType(entity);
        return Result.success();
    }

    @Operation(summary = "删除投诉类型")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody CommunityComplaintType entity) {
        if (entity.getId() == null) {
            throw new BizException("投诉类型ID不能为空");
        }
        CommunityComplaintType current = iCommunityComplaintTypeService.getById(entity.getId());
        if (current == null) {
            throw new BizException("投诉类型不存在");
        }
        long usedCount = countComplaintUsage(current.getName());
        if (usedCount > 0) {
            throw new BizException("该投诉类型已被使用，无法删除");
        }
        iCommunityComplaintTypeService.removeById(entity.getId());
        return Result.success();
    }

    private long countComplaintUsage(String typeName) {
        if (StringUtils.isBlank(typeName)) {
            return 0;
        }
        String trimmed = typeName.trim();
        String reasonPrefix = trimmed + "：";
        return iCommunityReportService.lambdaQuery()
                .and(wrapper -> wrapper.eq(CommunityReport::getReason, trimmed)
                        .or()
                        .likeRight(CommunityReport::getReason, reasonPrefix))
                .count();
    }

    private String validateName(String name, Long excludeId) {
        if (StringUtils.isBlank(name)) {
            throw new BizException("投诉类型名称不能为空");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 20) {
            throw new BizException("投诉类型名称不能超过20字");
        }
        long count = iCommunityComplaintTypeService.lambdaQuery()
                .eq(CommunityComplaintType::getName, trimmed)
                .ne(excludeId != null, CommunityComplaintType::getId, excludeId)
                .count();
        if (count > 0) {
            throw new BizException("投诉类型名称已存在");
        }
        return trimmed;
    }
}
