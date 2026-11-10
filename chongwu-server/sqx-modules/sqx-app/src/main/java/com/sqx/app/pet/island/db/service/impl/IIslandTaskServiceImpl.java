package com.sqx.app.pet.island.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.island.db.mapper.IslandTaskMapper;
import com.sqx.app.pet.island.db.service.IIslandTaskService;
import com.sqx.app.pet.island.domain.entity.IslandTask;
import org.springframework.stereotype.Service;

@Service
public class IIslandTaskServiceImpl extends ServiceImpl<IslandTaskMapper, IslandTask> implements IIslandTaskService {
}
