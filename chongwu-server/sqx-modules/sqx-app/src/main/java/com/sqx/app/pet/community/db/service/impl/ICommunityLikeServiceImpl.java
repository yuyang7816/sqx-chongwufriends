package com.sqx.app.pet.community.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.community.db.mapper.CommunityLikeMapper;
import com.sqx.app.pet.community.db.service.ICommunityLikeService;
import com.sqx.app.pet.community.domain.entity.CommunityLike;
import org.springframework.stereotype.Service;

@Service
public class ICommunityLikeServiceImpl extends ServiceImpl<CommunityLikeMapper, CommunityLike> implements ICommunityLikeService {
}
