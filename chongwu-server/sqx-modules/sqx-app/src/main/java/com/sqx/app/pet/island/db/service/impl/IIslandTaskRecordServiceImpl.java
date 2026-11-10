package com.sqx.app.pet.island.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.island.db.mapper.IslandTaskRecordMapper;
import com.sqx.app.pet.island.db.service.IIslandTaskRecordService;
import com.sqx.app.pet.island.domain.entity.IslandTaskRecord;
import org.springframework.stereotype.Service;

@Service
public class IIslandTaskRecordServiceImpl extends ServiceImpl<IslandTaskRecordMapper, IslandTaskRecord> implements IIslandTaskRecordService {
}
