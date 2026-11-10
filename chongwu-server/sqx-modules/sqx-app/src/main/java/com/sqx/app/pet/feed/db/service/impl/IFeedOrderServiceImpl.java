package com.sqx.app.pet.feed.db.service.impl;

import com.sqx.app.pet.feed.db.mapper.FeedOrderMapper;
import com.sqx.app.pet.feed.db.service.IFeedOrderService;
import com.sqx.app.pet.feed.domain.entity.FeedOrder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IFeedOrderServiceImpl extends ServiceImpl<FeedOrderMapper, FeedOrder> implements IFeedOrderService {
}
