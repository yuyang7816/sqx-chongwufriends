#!/usr/bin/env python3
"""Generate pet business CRUD boilerplate following sqx-app conventions."""
import os

BASE = os.path.join(os.path.dirname(__file__), "..", "sqx-modules", "sqx-app", "src", "main", "java", "com", "sqx", "app", "pet")

MODULES = [
    ("store", "PetStore", "pet_store", "门店"),
    ("archive", "PetArchive", "pet_archive", "宠物档案"),
    ("community", "CommunityPost", "community_post", "社区动态"),
    ("adopt", "AdoptApplication", "adopt_application", "领养申请"),
    ("mall", "MallSpu", "mall_spu", "商城商品"),
    ("asset", "UserAsset", "user_asset", "用户资产"),
    ("island", "IslandPet", "island_pet", "岛宅"),
    ("feed", "FeedOrder", "feed_order", "投喂订单"),
    ("marketing", "OperationBanner", "operation_banner", "运营位"),
    ("strategy", "PetStrategy", "pet_strategy", "养宠攻略"),
    ("city", "PetCity", "pet_city", "城市"),
]

def pkg(sub): return f"com.sqx.app.pet.{sub}"

def write(path, content):
    full = os.path.join(BASE, path.replace("/", os.sep))
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8") as f:
        f.write(content)

def gen_entity(sub, cls, table, desc):
    p = pkg(sub)
    write(f"{sub}/domain/entity/{cls}.java", f'''package {p}.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("{table}")
@Schema(name = "{cls}", description = "{desc}")
public class {cls} implements Serializable {{
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
}}
''')

def gen_bo(sub, cls, bo, parent=False):
    p = pkg(sub)
    ext = " extends com.sqx.common.mybatis.domain.PageParam" if parent else ""
    imp = "import com.sqx.common.mybatis.domain.PageParam;\n" if parent else ""
    fields = "    private Long id;\n" if bo != "Save" else ""
    write(f"{sub}/domain/bo/{cls}{bo}Bo.java", f'''package {p}.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
{imp}

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "{cls}{bo}Bo")
public class {cls}{bo}Bo{ext} {{
{fields}}}
''')

def gen_vo(sub, cls):
    p = pkg(sub)
    write(f"{sub}/domain/vo/{cls}Vo.java", f'''package {p}.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "{cls}Vo")
public class {cls}Vo implements Serializable {{
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}}
''')

def gen_mapper(sub, cls):
    p = pkg(sub)
    write(f"{sub}/db/mapper/{cls}Mapper.java", f'''package {p}.db.mapper;

import {p}.domain.entity.{cls};
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface {cls}Mapper extends BaseMapper<{cls}> {{
}}
''')

def gen_db_service(sub, cls):
    p = pkg(sub)
    write(f"{sub}/db/service/I{cls}Service.java", f'''package {p}.db.service;

import {p}.domain.entity.{cls};
import com.baomidou.mybatisplus.extension.service.IService;

public interface I{cls}Service extends IService<{cls}> {{
}}
''')
    write(f"{sub}/db/service/impl/I{cls}ServiceImpl.java", f'''package {p}.db.service.impl;

import {p}.db.mapper.{cls}Mapper;
import {p}.db.service.I{cls}Service;
import {p}.domain.entity.{cls};
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class I{cls}ServiceImpl extends ServiceImpl<{cls}Mapper, {cls}> implements I{cls}Service {{
}}
''')

def gen_service(sub, cls, desc):
    p = pkg(sub)
    write(f"{sub}/service/{cls}Service.java", f'''package {p}.service;

import {p}.domain.bo.*;
import {p}.domain.vo.{cls}Vo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface {cls}Service {{
    void saveInfo({cls}SaveBo bo);
    IPage<{cls}Vo> pageInfo({cls}PageBo bo);
    {cls}Vo info({cls}InfoBo bo);
    void updateInfo({cls}UpdateBo bo);
    void delete({cls}InfoBo bo);
    List<{cls}Vo> list();
}}
''')
    write(f"{sub}/service/impl/{cls}ServiceImpl.java", f'''package {p}.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import {p}.db.service.I{cls}Service;
import {p}.domain.bo.*;
import {p}.domain.entity.{cls};
import {p}.domain.vo.{cls}Vo;
import {p}.service.{cls}Service;
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
public class {cls}ServiceImpl implements {cls}Service {{
    @Resource
    private I{cls}Service i{cls}Service;

    @Override
    public void saveInfo({cls}SaveBo bo) {{
        {cls} entity = new {cls}();
        BeanUtils.copyProperties(bo, entity);
        i{cls}Service.save(entity);
    }}

    @Override
    public IPage<{cls}Vo> pageInfo({cls}PageBo bo) {{
        Page<{cls}> page = PageUtil.initPage(bo.getPage(), bo.getLimit());
        QueryWrapper<{cls}> qw = new QueryWrapper<>();
        qw.lambda().orderByDesc({cls}::getCreateTime);
        IPage<{cls}> iPage = i{cls}Service.page(page, qw);
        List<{cls}Vo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {{
            for ({cls} record : iPage.getRecords()) {{
                {cls}Vo vo = new {cls}Vo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }}
        }}
        return PageUtil.copyPage(iPage, voList);
    }}

    @Override
    public {cls}Vo info({cls}InfoBo bo) {{
        {cls} entity = i{cls}Service.getById(bo.getId());
        if (Objects.isNull(entity)) {{
            throw new BizException("信息不存在");
        }}
        {cls}Vo vo = new {cls}Vo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }}

    @Override
    public void updateInfo({cls}UpdateBo bo) {{
        {cls} entity = new {cls}();
        BeanUtils.copyProperties(bo, entity);
        i{cls}Service.updateById(entity);
    }}

    @Override
    public void delete({cls}InfoBo bo) {{
        i{cls}Service.removeById(bo.getId());
    }}

    @Override
    public List<{cls}Vo> list() {{
        List<{cls}> list = i{cls}Service.list();
        if (CollectionUtils.isEmpty(list)) {{
            return Collections.emptyList();
        }}
        return list.stream().map(e -> {{
            {cls}Vo vo = new {cls}Vo();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }}).collect(Collectors.toList());
    }}
}}
''')

def gen_controllers(sub, cls, desc, app_path):
    p = pkg(sub)
    admin_path = f"/admin/pet/{app_path}"
    app_url = f"/app/pet/{app_path}"
    write(f"{sub}/controller/Admin{cls}Controller.java", f'''package {p}.controller;

import {p}.domain.bo.*;
import {p}.domain.vo.{cls}Vo;
import {p}.service.{cls}Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Tag(name = "管理端-{desc}")
@RestController
@RequestMapping("{admin_path}")
public class Admin{cls}Controller {{
    @Resource
    private {cls}Service {cls[0].lower() + cls[1:]}Service;

    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated {cls}SaveBo bo) {{
        {cls[0].lower() + cls[1:]}Service.saveInfo(bo);
        return Result.success();
    }}

    @Operation(summary = "分页")
    @PostMapping("page")
    public Result<IPage<{cls}Vo>> page(@RequestBody @Validated {cls}PageBo bo) {{
        return Result.success({cls[0].lower() + cls[1:]}Service.pageInfo(bo));
    }}

    @Operation(summary = "详情")
    @PostMapping("info")
    public Result<{cls}Vo> info(@RequestBody @Validated {cls}InfoBo bo) {{
        return Result.success({cls[0].lower() + cls[1:]}Service.info(bo));
    }}

    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated {cls}UpdateBo bo) {{
        {cls[0].lower() + cls[1:]}Service.updateInfo(bo);
        return Result.success();
    }}

    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated {cls}InfoBo bo) {{
        {cls[0].lower() + cls[1:]}Service.delete(bo);
        return Result.success();
    }}
}}
''')
    write(f"{sub}/controller/App{cls}Controller.java", f'''package {p}.controller;

import {p}.domain.bo.*;
import {p}.domain.vo.{cls}Vo;
import {p}.service.{cls}Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "用户端-{desc}")
@RestController
@RequestMapping("{app_url}")
public class App{cls}Controller {{
    @Resource
    private {cls}Service {cls[0].lower() + cls[1:]}Service;

    @Operation(summary = "列表")
    @PostMapping("list")
    public Result<List<{cls}Vo>> list() {{
        return Result.success({cls[0].lower() + cls[1:]}Service.list());
    }}

    @Operation(summary = "分页")
    @PostMapping("page")
    public Result<IPage<{cls}Vo>> page(@RequestBody @Validated {cls}PageBo bo) {{
        return Result.success({cls[0].lower() + cls[1:]}Service.pageInfo(bo));
    }}

    @Operation(summary = "详情")
    @PostMapping("info")
    public Result<{cls}Vo> info(@RequestBody @Validated {cls}InfoBo bo) {{
        return Result.success({cls[0].lower() + cls[1:]}Service.info(bo));
    }}
}}
''')

def main():
    for sub, cls, table, desc in MODULES:
        app_path = sub if sub not in ("strategy",) else "strategy"
        if sub == "marketing":
            app_path = "banner"
        gen_entity(sub, cls, table, desc)
        for bo in ["Save", "Update", "Info"]:
            gen_bo(sub, cls, bo)
        gen_bo(sub, cls, "Page", parent=True)
        gen_vo(sub, cls)
        gen_mapper(sub, cls)
        gen_db_service(sub, cls)
        gen_service(sub, cls, desc)
        path = app_path
        gen_controllers(sub, cls, desc, path)
        print(f"Generated {sub}")

if __name__ == "__main__":
    main()
