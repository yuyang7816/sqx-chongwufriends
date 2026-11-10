package com.sqx.app.pet.feed.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.feed.db.mapper.FeedOrderMessageMapper;
import com.sqx.app.pet.feed.db.service.IFeedOrderMessageService;
import com.sqx.app.pet.feed.domain.entity.FeedOrderMessage;
import org.springframework.stereotype.Service;

@Service
public class IFeedOrderMessageServiceImpl extends ServiceImpl<FeedOrderMessageMapper, FeedOrderMessage> implements IFeedOrderMessageService {
}
