package com.sqx.app.pet.feed.service;

import com.sqx.app.pet.feed.domain.bo.*;
import com.sqx.app.pet.feed.domain.vo.FeedGiftSummaryVo;
import com.sqx.app.pet.feed.domain.vo.FeedOrderVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface FeedOrderService {
    void saveInfo(FeedOrderSaveBo bo);
    IPage<FeedOrderVo> pageInfo(FeedOrderPageBo bo);
    FeedOrderVo info(FeedOrderInfoBo bo);
    void updateInfo(FeedOrderUpdateBo bo);
    void delete(FeedOrderInfoBo bo);
    List<FeedOrderVo> list();

    FeedOrderVo createForUser(Long userId, FeedOrderCreateBo bo);

    IPage<FeedOrderVo> listMyGifts(Long userId, FeedOrderPageBo bo);

    void uploadFeedback(Long userId, FeedFeedbackUploadBo bo);

    FeedGiftSummaryVo giftSummary(Long userId);

    void adminUploadFeedback(FeedFeedbackUploadBo bo);
}
