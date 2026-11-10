package com.sqx.app.pet.community.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.pet.community.db.mapper.CommunityReportMapper;
import com.sqx.app.pet.community.db.service.ICommunityReportService;
import com.sqx.app.pet.community.domain.entity.CommunityReport;
import org.springframework.stereotype.Service;

@Service
public class ICommunityReportServiceImpl extends ServiceImpl<CommunityReportMapper, CommunityReport> implements ICommunityReportService {
}
