package com.sqx.app.pet.userpet.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.userpet.db.mapper.UserPetMapper;
import com.sqx.app.pet.userpet.db.service.IUserPetService;
import com.sqx.app.pet.userpet.domain.entity.UserPet;
import org.springframework.stereotype.Service;

@Service
public class IUserPetServiceImpl extends ServiceImpl<UserPetMapper, UserPet> implements IUserPetService {
}
