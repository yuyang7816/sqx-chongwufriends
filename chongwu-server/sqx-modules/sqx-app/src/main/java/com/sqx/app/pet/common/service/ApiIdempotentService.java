package com.sqx.app.pet.common.service;

/**
 * 接口幂等服务
 */
public interface ApiIdempotentService {

    /**
     * 校验幂等键，若已存在且未过期则返回 true（表示重复请求）
     */
    boolean isDuplicate(String idempotentKey, String apiPath, Long userId);

    /**
     * 记录幂等结果
     */
    void record(String idempotentKey, String apiPath, Long userId, String resultSnapshot, int expireSeconds);
}
