package com.sqx.app.pet.user.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.user.db.mapper.UserCancelApplyMapper;
import com.sqx.app.pet.user.db.service.IUserCancelApplyService;
import com.sqx.app.pet.user.domain.entity.UserCancelApply;
import org.springframework.stereotype.Service;

@Service
public class IUserCancelApplyServiceImpl extends ServiceImpl<UserCancelApplyMapper, UserCancelApply> implements IUserCancelApplyService {
}
