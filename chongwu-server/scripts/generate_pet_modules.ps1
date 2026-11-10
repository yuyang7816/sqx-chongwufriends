$Base = Join-Path $PSScriptRoot "..\sqx-modules\sqx-app\src\main\java\com\sqx\app\pet"
$Modules = @(
    @("store","PetStore","pet_store","门店"),
    @("archive","PetArchive","pet_archive","宠物档案"),
    @("community","CommunityPost","community_post","社区动态"),
    @("adopt","AdoptApplication","adopt_application","领养申请"),
    @("mall","MallSpu","mall_spu","商城商品"),
    @("asset","UserAsset","user_asset","用户资产"),
    @("island","IslandPet","island_pet","岛宅"),
    @("feed","FeedOrder","feed_order","投喂订单"),
    @("marketing","OperationBanner","operation_banner","运营位"),
    @("strategy","PetStrategy","pet_strategy","养宠攻略"),
    @("city","PetCity","pet_city","城市")
)

function Write-File($rel, $content) {
    $full = Join-Path $Base $rel
    $dir = Split-Path $full -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    Set-Content -Path $full -Value $content -Encoding UTF8
}

foreach ($m in $Modules) {
    $sub, $cls, $table, $desc = $m
    $pkg = "com.sqx.app.pet.$sub"
    $svcVar = $cls.Substring(0,1).ToLower() + $cls.Substring(1)
    $appPath = if ($sub -eq "marketing") { "banner" } elseif ($sub -eq "strategy") { "strategy" } else { $sub }

    Write-File "$sub/domain/entity/$cls.java" @"
package $pkg.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter @Setter
@TableName("$table")
@Schema(name = "$cls", description = "$desc")
public class $cls implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO) private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT) private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE) private LocalDateTime updateTime;
    @TableLogic @TableField("is_deleted") private Integer isDeleted;
}
"@

    foreach ($bo in @("Save","Update","Info")) {
        $idField = if ($bo -ne "Save") { "    private Long id;`n" } else { "" }
        Write-File "$sub/domain/bo/${cls}${bo}Bo.java" @"
package $pkg.domain.bo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Data @Schema(description = "${cls}${bo}Bo")
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
@Data @EqualsAndHashCode(callSuper = true) @Schema(description = "${cls}PageBo")
public class ${cls}PageBo extends PageParam {}
"@

    Write-File "$sub/domain/vo/${cls}Vo.java" @"
package $pkg.domain.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
@Data @Schema(description = "${cls}Vo")
public class ${cls}Vo implements Serializable {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private LocalDateTime updateTime;
}
"@

    Write-File "$sub/db/mapper/${cls}Mapper.java" @"
package $pkg.db.mapper;
import $pkg.domain.entity.$cls;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface ${cls}Mapper extends BaseMapper<$cls> {}
"@

    Write-File "$sub/db/service/I${cls}Service.java" @"
package $pkg.db.service;
import $pkg.domain.entity.$cls;
import com.baomidou.mybatisplus.extension.service.IService;
public interface I${cls}Service extends IService<$cls> {}
"@

    Write-File "$sub/db/service/impl/I${cls}ServiceImpl.java" @"
package $pkg.db.service.impl;
import $pkg.db.mapper.${cls}Mapper;
import $pkg.db.service.I${cls}Service;
import $pkg.domain.entity.$cls;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
@Service
public class I${cls}ServiceImpl extends ServiceImpl<${cls}Mapper, $cls> implements I${cls}Service {}
"@

    # Service interface + impl - abbreviated via here-string in next block
    Write-Host "Generated $sub"
}

Write-Host "Done base entities"
