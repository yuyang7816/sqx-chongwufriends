package com.sqx.app.pet.common.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.common.db.mapper.UserAssetLedgerMapper;
import com.sqx.app.pet.common.db.service.IUserAssetLedgerService;
import com.sqx.app.pet.common.domain.entity.UserAssetLedger;
import org.springframework.stereotype.Service;

@Service
public class IUserAssetLedgerServiceImpl extends ServiceImpl<UserAssetLedgerMapper, UserAssetLedger> implements IUserAssetLedgerService {
}
