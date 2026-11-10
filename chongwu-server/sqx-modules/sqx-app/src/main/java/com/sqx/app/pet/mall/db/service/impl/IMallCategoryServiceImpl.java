package com.sqx.app.pet.mall.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.mall.db.mapper.MallCategoryMapper;
import com.sqx.app.pet.mall.db.service.IMallCategoryService;
import com.sqx.app.pet.mall.domain.entity.MallCategory;
import org.springframework.stereotype.Service;

@Service
public class IMallCategoryServiceImpl extends ServiceImpl<MallCategoryMapper, MallCategory> implements IMallCategoryService {
}
