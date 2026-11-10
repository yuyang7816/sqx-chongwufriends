package com.sqx.app.pet.island.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.archive.domain.vo.PetArchiveVo;
import com.sqx.app.pet.island.domain.bo.IslandBuyMarketBo;
import com.sqx.app.pet.island.domain.bo.IslandFeedBo;
import com.sqx.app.pet.island.domain.bo.IslandMarketRecordPageBo;
import com.sqx.app.pet.island.domain.bo.IslandResidentSaveBo;
import com.sqx.app.pet.island.domain.vo.IslandMarketItemVo;
import com.sqx.app.pet.island.domain.vo.IslandMarketRecordVo;
import com.sqx.app.pet.island.domain.vo.IslandPetVo;

import java.util.List;

public interface IslandService {
    IslandPetVo getMyIsland(Long userId);
    IslandPetVo feed(Long userId, IslandFeedBo bo);
    IslandPetVo buyMarketItem(Long userId, IslandBuyMarketBo bo);
    List<IslandMarketItemVo> listMarketItems();
    IPage<IslandMarketRecordVo> listMarketRecords(Long userId, IslandMarketRecordPageBo bo);
    IslandPetVo saveResidents(Long userId, IslandResidentSaveBo bo);
    /** 常驻可选：当前用户自有宠物 */
    List<PetArchiveVo> listResidentCandidates(Long userId);
}
