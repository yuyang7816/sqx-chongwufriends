package com.sqx.app.pet.common.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.pet.common.domain.entity.ApiIdempotent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiIdempotentMapper extends BaseMapper<ApiIdempotent> {
}
