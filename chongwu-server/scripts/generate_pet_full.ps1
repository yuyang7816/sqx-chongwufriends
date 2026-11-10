$Base = Join-Path $PSScriptRoot "..\sqx-modules\sqx-app\src\main\java\com\sqx\app\pet"
$Modules = @(
    @("store","PetStore","pet_store","Pet Store","store"),
    @("archive","PetArchive","pet_archive","Pet Archive","archive"),
    @("community","CommunityPost","community_post","Community Post","community"),
    @("adopt","AdoptApplication","adopt_application","Adopt Application","adopt"),
    @("mall","MallSpu","mall_spu","Mall SPU","mall"),
    @("asset","UserAsset","user_asset","User Asset","asset"),
    @("island","IslandPet","island_pet","Island Pet","island"),
    @("feed","FeedOrder","feed_order","Feed Order","feed"),
    @("marketing","OperationBanner","operation_banner","Operation Banner","banner"),
    @("strategy","PetStrategy","pet_strategy","Pet Strategy","strategy"),
    @("city","PetCity","pet_city","Pet City","city")
)

function Write-File($rel, $content) {
    $full = Join-Path $Base $rel
    $dir = Split-Path $full -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    [System.IO.File]::WriteAllText($full, $content, [System.Text.UTF8Encoding]::new($false))
}

foreach ($m in $Modules) {
    $sub, $cls, $table, $desc, $appPath = $m
    $pkg = "com.sqx.app.pet.$sub"
    $svcVar = $cls.Substring(0,1).ToLower() + $cls.Substring(1)
    $adminPath = "/admin/pet/$appPath"
    $appUrl = "/app/pet/$appPath"

    Write-File "$sub/domain/entity/$cls.java" @"
package $pkg.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("$table")
@Schema(name = "$cls", description = "$desc")
public class $cls implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}

"@

    foreach ($bo in @("Save","Update","Info")) {
        $idField = if ($bo -ne "Save") { "    private Long id;`n" } else { "" }
        Write-File "$sub/domain/bo/${cls}${bo}Bo.java" @"
package $pkg.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "${cls}${bo}Bo")
public class ${cls}${bo}Bo {
$idField}

"@
    }
    Write-File "$sub/domain/bo/${cls}PageBo.java" @"
package $pkg.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "${cls}PageBo")
public class ${cls}PageBo extends PageParam {
}

"@
    Write-File "$sub/domain/vo/${cls}Vo.java" @"
package $pkg.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "${cls}Vo")
public class ${cls}Vo implements Serializable {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

"@
    Write-File "$sub/db/mapper/${cls}Mapper.java" @"
package $pkg.db.mapper;

import $pkg.domain.entity.$cls;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ${cls}Mapper extends BaseMapper<$cls> {
}

"@
    Write-File "$sub/db/service/I${cls}Service.java" @"
package $pkg.db.service;

import $pkg.domain.entity.$cls;
import com.baomidou.mybatisplus.extension.service.IService;

public interface I${cls}Service extends IService<$cls> {
}

"@
    Write-File "$sub/db/service/impl/I${cls}ServiceImpl.java" @"
package $pkg.db.service.impl;

import $pkg.db.mapper.${cls}Mapper;
import $pkg.db.service.I${cls}Service;
import $pkg.domain.entity.$cls;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class I${cls}ServiceImpl extends ServiceImpl<${cls}Mapper, $cls> implements I${cls}Service {
}

"@
    Write-File "$sub/service/${cls}Service.java" @"
package $pkg.service;

import $pkg.domain.bo.*;
import $pkg.domain.vo.${cls}Vo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface ${cls}Service {
    void saveInfo(${cls}SaveBo bo);
    IPage<${cls}Vo> pageInfo(${cls}PageBo bo);
    ${cls}Vo info(${cls}InfoBo bo);
    void updateInfo(${cls}UpdateBo bo);
    void delete(${cls}InfoBo bo);
    List<${cls}Vo> list();
}

"@
    Write-File "$sub/service/impl/${cls}ServiceImpl.java" @"
package $pkg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import $pkg.db.service.I${cls}Service;
import $pkg.domain.bo.*;
import $pkg.domain.entity.$cls;
import $pkg.domain.vo.${cls}Vo;
import $pkg.service.${cls}Service;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ${cls}ServiceImpl implements ${cls}Service {

    @Resource
    private I${cls}Service i${cls}Service;

    @Override
    public void saveInfo(${cls}SaveBo bo) {
        $cls entity = new $cls();
        BeanUtils.copyProperties(bo, entity);
        i${cls}Service.save(entity);
    }

    @Override
    public IPage<${cls}Vo> pageInfo(${cls}PageBo bo) {
        Page<$cls> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<$cls> qw = new QueryWrapper<>();
        qw.lambda().orderByDesc($cls::getCreateTime);
        IPage<$cls> iPage = i${cls}Service.page(page, qw);
        List<${cls}Vo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for ($cls record : iPage.getRecords()) {
                ${cls}Vo vo = new ${cls}Vo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public ${cls}Vo info(${cls}InfoBo bo) {
        $cls entity = i${cls}Service.getById(bo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException("not found");
        }
        ${cls}Vo vo = new ${cls}Vo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void updateInfo(${cls}UpdateBo bo) {
        $cls entity = new $cls();
        BeanUtils.copyProperties(bo, entity);
        i${cls}Service.updateById(entity);
    }

    @Override
    public void delete(${cls}InfoBo bo) {
        i${cls}Service.removeById(bo.getId());
    }

    @Override
    public List<${cls}Vo> list() {
        List<$cls> list = i${cls}Service.list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(e -> {
            ${cls}Vo vo = new ${cls}Vo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}

"@
    Write-File "$sub/controller/Admin${cls}Controller.java" @"
package $pkg.controller;

import $pkg.domain.bo.*;
import $pkg.domain.vo.${cls}Vo;
import $pkg.service.${cls}Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.common.core.domain.Result;
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

@Tag(name = "Admin-$desc", description = "Admin-$desc")
@RestController
@RequestMapping("$adminPath")
public class Admin${cls}Controller {

    @Resource
    private ${cls}Service $svcVar;

    @Operation(summary = "save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated ${cls}SaveBo bo) {
        $svcVar.saveInfo(bo);
        return Result.success();
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ${cls}Vo.class)))
    @PostMapping("page")
    public Result<IPage<${cls}Vo>> page(@RequestBody @Validated ${cls}PageBo bo) {
        return Result.success($svcVar.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ${cls}Vo.class)))
    @PostMapping("info")
    public Result<${cls}Vo> info(@RequestBody @Validated ${cls}InfoBo bo) {
        return Result.success($svcVar.info(bo));
    }

    @Operation(summary = "update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated ${cls}UpdateBo bo) {
        $svcVar.updateInfo(bo);
        return Result.success();
    }

    @Operation(summary = "delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated ${cls}InfoBo bo) {
        $svcVar.delete(bo);
        return Result.success();
    }
}

"@
    Write-File "$sub/controller/App${cls}Controller.java" @"
package $pkg.controller;

import $pkg.domain.bo.*;
import $pkg.domain.vo.${cls}Vo;
import $pkg.service.${cls}Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.common.core.domain.Result;
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
import java.util.List;

@Tag(name = "App-$desc", description = "App-$desc")
@RestController
@RequestMapping("$appUrl")
public class App${cls}Controller {

    @Resource
    private ${cls}Service $svcVar;

    @Operation(summary = "list")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ${cls}Vo.class)))
    @PostMapping("list")
    public Result<List<${cls}Vo>> list() {
        return Result.success($svcVar.list());
    }

    @Operation(summary = "page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ${cls}Vo.class)))
    @PostMapping("page")
    public Result<IPage<${cls}Vo>> page(@RequestBody @Validated ${cls}PageBo bo) {
        return Result.success($svcVar.pageInfo(bo));
    }

    @Operation(summary = "info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ${cls}Vo.class)))
    @PostMapping("info")
    public Result<${cls}Vo> info(@RequestBody @Validated ${cls}InfoBo bo) {
        return Result.success($svcVar.info(bo));
    }
}

"@
    Write-Host "Generated $sub"
}
Write-Host "Done"
