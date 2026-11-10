package com.sqx.app.pet.community.db.service.impl;

import com.sqx.app.pet.community.db.mapper.CommunityPostMapper;
import com.sqx.app.pet.community.db.service.ICommunityPostService;
import com.sqx.app.pet.community.domain.entity.CommunityPost;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ICommunityPostServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost> implements ICommunityPostService {
}
