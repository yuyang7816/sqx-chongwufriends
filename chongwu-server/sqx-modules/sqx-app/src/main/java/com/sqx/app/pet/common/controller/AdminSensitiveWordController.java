package com.sqx.app.pet.common.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.pet.common.domain.entity.SensitiveWord;
import com.sqx.app.pet.common.db.service.ISensitiveWordService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.domain.PageParam;
import com.sqx.common.mybatis.utils.PageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

@Tag(name = "管理端-敏感词")
@RestController
@RequestMapping("/admin/pet/sensitive")
public class AdminSensitiveWordController {

    @Resource
    private ISensitiveWordService iSensitiveWordService;

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class WordPageBo extends PageParam {
        private String word;
    }

    @Data
    public static class WordSaveBo {
        private Long id;
        private String word;
        private Integer status = 1;
    }

    @Data
    public static class IdBo {
        private Long id;
    }

    @Operation(summary = "分页")
    @PostMapping("page")
    public Result<IPage<SensitiveWord>> page(@RequestBody @Validated WordPageBo bo) {
        Page<SensitiveWord> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<SensitiveWord> qw = new QueryWrapper<>();
        qw.lambda().like(StringUtils.isNotBlank(bo.getWord()), SensitiveWord::getWord, bo.getWord())
                .orderByDesc(SensitiveWord::getCreateTime);
        return Result.success(iSensitiveWordService.page(page, qw));
    }

    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestBody WordSaveBo bo) {
        SensitiveWord entity = new SensitiveWord();
        entity.setWord(bo.getWord());
        entity.setStatus(bo.getStatus());
        iSensitiveWordService.save(entity);
        return Result.success();
    }

    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody WordSaveBo bo) {
        SensitiveWord entity = iSensitiveWordService.getById(bo.getId());
        if (Objects.isNull(entity)) throw new BizException("not found");
        entity.setWord(bo.getWord());
        entity.setStatus(bo.getStatus());
        iSensitiveWordService.updateById(entity);
        return Result.success();
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody IdBo bo) {
        iSensitiveWordService.removeById(bo.getId());
        return Result.success();
    }
}
