package com.sqx.app.pet.island.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.island.db.mapper.IslandMarketItemMapper;
import com.sqx.app.pet.island.db.service.IIslandMarketItemService;
import com.sqx.app.pet.island.domain.entity.IslandMarketItem;
import org.springframework.stereotype.Service;

@Service
public class IIslandMarketItemServiceImpl extends ServiceImpl<IslandMarketItemMapper, IslandMarketItem> implements IIslandMarketItemService {
}
