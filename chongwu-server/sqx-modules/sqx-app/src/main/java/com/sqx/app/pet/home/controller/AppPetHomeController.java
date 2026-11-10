package com.sqx.app.pet.home.controller;

import com.sqx.app.pet.home.domain.bo.BannerClickBo;
import com.sqx.app.pet.home.domain.vo.HomeConfigVo;
import com.sqx.app.pet.home.service.HomeConfigService;
import com.sqx.app.pet.archive.domain.vo.PetArchiveVo;
import com.sqx.app.pet.archive.service.PetArchiveService;
import com.sqx.app.pet.city.domain.vo.PetCityVo;
import com.sqx.app.pet.city.service.PetCityService;
import com.sqx.app.pet.marketing.domain.vo.OperationBannerVo;
import com.sqx.app.pet.marketing.service.OperationBannerService;
import com.sqx.app.pet.mall.db.service.IMallCategoryService;
import com.sqx.app.pet.mall.domain.entity.MallCategory;
import com.sqx.app.pet.store.domain.vo.PetStoreVo;
import com.sqx.app.pet.store.service.PetStoreService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "用户端-宠物首页", description = "宠物首页聚合接口")
@RestController
@RequestMapping("/app/pet/home")
public class AppPetHomeController {

    @Resource
    private PetCityService petCityService;
    @Resource
    private PetStoreService petStoreService;
    @Resource
    private PetArchiveService petArchiveService;
    @Resource
    private OperationBannerService operationBannerService;
    @Resource
    private IMallCategoryService iMallCategoryService;
    @Resource
    private HomeConfigService homeConfigService;

    @Operation(summary = "首页配置(社群入口等)")
    @GetMapping("config")
    public Result<HomeConfigVo> config() {
        return Result.success(homeConfigService.getConfig());
    }

    @Operation(summary = "Banner点击上报")
    @PostMapping("banner/click")
    public Result<Void> bannerClick(@RequestBody @Validated BannerClickBo bo) {
        operationBannerService.incrementClick(bo.getBannerId());
        return Result.success();
    }

    @Operation(summary = "商城分类(首页/商城Tab)")
    @GetMapping("categories")
    public Result<List<MallCategory>> categories() {
        List<MallCategory> list = iMallCategoryService.lambdaQuery()
                .eq(MallCategory::getStatus, 1)
                .orderByAsc(MallCategory::getSort)
                .list();
        if (list == null || list.isEmpty()) {
            list = defaultCategories();
        }
        return Result.success(list);
    }

    private List<MallCategory> defaultCategories() {
        List<MallCategory> list = new ArrayList<>();
        list.add(category(1L, "全部商品", 0));
        list.add(category(2L, "客户投喂", 1));
        list.add(category(3L, "超值次卡", 2));
        list.add(category(4L, "实物赠予", 3));
        return list;
    }

    private MallCategory category(Long id, String name, int sort) {
        MallCategory c = new MallCategory();
        c.setId(id);
        c.setName(name);
        c.setSort(sort);
        c.setStatus(1);
        return c;
    }

    @Operation(summary = "城市列表")
    @GetMapping("cities")
    public Result<List<PetCityVo>> cities() {
        return Result.success(petCityService.listEnabled());
    }

    @Operation(summary = "门店列表")
    @GetMapping("stores")
    public Result<List<PetStoreVo>> stores(@RequestParam("cityId") Long cityId) {
        return Result.success(petStoreService.listByCityId(cityId));
    }

    @Operation(summary = "宠物列表")
    @GetMapping("pets")
    public Result<List<PetArchiveVo>> pets(@RequestParam("storeId") Long storeId,
                                           @RequestParam(value = "petType", required = false) String petType) {
        return Result.success(petArchiveService.listByStoreId(storeId, petType));
    }

    @Operation(summary = "运营位Banner")
    @GetMapping("banners")
    public Result<List<OperationBannerVo>> banners(@RequestParam(value = "positionCode", defaultValue = "home") String positionCode) {
        return Result.success(operationBannerService.listByPositionCode(positionCode));
    }
}
