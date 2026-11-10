package com.sqx.app.pet.community.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.community.db.mapper.CommunityCommentMapper;
import com.sqx.app.pet.community.db.service.ICommunityCommentService;
import com.sqx.app.pet.community.domain.entity.CommunityComment;
import org.springframework.stereotype.Service;

@Service
public class ICommunityCommentServiceImpl extends ServiceImpl<CommunityCommentMapper, CommunityComment> implements ICommunityCommentService {
}
