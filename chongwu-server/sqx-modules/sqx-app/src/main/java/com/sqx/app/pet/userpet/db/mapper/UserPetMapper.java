package com.sqx.app.pet.userpet.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.pet.userpet.domain.entity.UserPet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPetMapper extends BaseMapper<UserPet> {
}
