package com.sqx.common.core.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 手机号脱敏工具
 */
public final class PhoneMaskUtil {

    private PhoneMaskUtil() {
    }

    /**
     * 若 userName 为 11 位手机号，中间四位替换为 ****
     */
    public static String maskUserName(String userName) {
        if (StringUtils.isBlank(userName)) {
            return userName;
        }
        String trimmed = userName.trim();
        if (trimmed.matches("^1\\d{10}$")) {
            return trimmed.substring(0, 3) + "****" + trimmed.substring(7);
        }
        return userName;
    }
}
