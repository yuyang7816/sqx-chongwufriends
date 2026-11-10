package com.sqx.app.pet.common.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.common.db.mapper.SensitiveWordMapper;
import com.sqx.app.pet.common.db.service.ISensitiveWordService;
import com.sqx.app.pet.common.domain.entity.SensitiveWord;
import org.springframework.stereotype.Service;

@Service
public class ISensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord> implements ISensitiveWordService {
}
