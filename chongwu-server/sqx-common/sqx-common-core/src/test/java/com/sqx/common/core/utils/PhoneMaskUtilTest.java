package com.sqx.common.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PhoneMaskUtilTest {

    @Test
    void maskBlankReturnsAsIs() {
        assertNull(PhoneMaskUtil.maskUserName(null));
        assertEquals("", PhoneMaskUtil.maskUserName(""));
        assertEquals("  ", PhoneMaskUtil.maskUserName("  "));
    }

    @Test
    void maskPhoneNumber() {
        assertEquals("138****8000", PhoneMaskUtil.maskUserName("13800138000"));
        assertEquals("138****8000", PhoneMaskUtil.maskUserName("13800138000 "));
    }

    @Test
    void nonPhoneUnchanged() {
        assertEquals("nickname", PhoneMaskUtil.maskUserName("nickname"));
        assertEquals("1234567890", PhoneMaskUtil.maskUserName("1234567890"));
    }
}
