package com.sqx.app.pet.marketing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.pet.marketing.domain.bo.AssistPageBo;
import com.sqx.app.pet.marketing.domain.bo.RankingAssistBo;
import com.sqx.app.pet.marketing.domain.bo.RankingAssistSettingsBo;
import com.sqx.app.pet.marketing.domain.bo.RankingPageBo;
import com.sqx.app.pet.marketing.domain.vo.AssistSummaryVo;
import com.sqx.app.pet.marketing.domain.vo.MarketingAssistVo;
import com.sqx.app.pet.marketing.domain.vo.MarketingRankVo;
import com.sqx.app.pet.marketing.domain.vo.RankingAssistQuotaVo;
import com.sqx.app.pet.marketing.domain.vo.RankingAssistSettingsVo;
import com.sqx.app.pet.marketing.domain.vo.RankingPeriodOverviewVo;

import java.util.List;

public interface MarketingRankService {
    IPage<MarketingRankVo> currentRanking(RankingPageBo bo);
    void assist(Long userId, RankingAssistBo bo);
    List<MarketingAssistVo> myAssistRecords(Long userId);

    IPage<MarketingAssistVo> myAssistPage(Long userId, AssistPageBo bo);

    AssistSummaryVo assistSummary(Long userId);

    RankingAssistQuotaVo assistQuota(Long userId);

    RankingPeriodOverviewVo periodOverview();

    String getRankingRules();

    void updateRankingRules(String rules);

    RankingAssistSettingsVo getAssistSettings();

    void updateAssistSettings(RankingAssistSettingsBo bo);
}
