package com.sqx.app.pet.feed.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.feed.db.mapper.FeedFeedbackMapper;
import com.sqx.app.pet.feed.db.service.IFeedFeedbackService;
import com.sqx.app.pet.feed.domain.entity.FeedFeedback;
import org.springframework.stereotype.Service;

@Service
public class IFeedFeedbackServiceImpl extends ServiceImpl<FeedFeedbackMapper, FeedFeedback> implements IFeedFeedbackService {
}
