package com.sqx.app.pet.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.pet.common.db.service.IApiIdempotentService;
import com.sqx.app.pet.common.domain.entity.ApiIdempotent;
import com.sqx.app.pet.common.service.ApiIdempotentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class ApiIdempotentServiceImpl implements ApiIdempotentService {

    @Resource
    private IApiIdempotentService iApiIdempotentService;

    @Override
    public boolean isDuplicate(String idempotentKey, String apiPath, Long userId) {
        if (StringUtils.isBlank(idempotentKey)) {
            return false;
        }
        QueryWrapper<ApiIdempotent> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(ApiIdempotent::getIdempotentKey, idempotentKey)
                .eq(ApiIdempotent::getApiPath, apiPath)
                .gt(ApiIdempotent::getExpireTime, LocalDateTime.now());
        if (Objects.nonNull(userId)) {
            qw.lambda().eq(ApiIdempotent::getUserId, userId);
        }
        return iApiIdempotentService.count(qw) > 0;
    }

    @Override
    public void record(String idempotentKey, String apiPath, Long userId, String resultSnapshot, int expireSeconds) {
        if (StringUtils.isBlank(idempotentKey)) {
            return;
        }
        ApiIdempotent record = new ApiIdempotent();
        record.setIdempotentKey(idempotentKey);
        record.setApiPath(apiPath);
        record.setUserId(userId);
        record.setResultSnapshot(resultSnapshot);
        record.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        iApiIdempotentService.save(record);
    }
}
