package com.sqx.common.core.domain;

import com.sqx.common.core.enums.SystemErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTest {

    @Test
    void successWithoutData() {
        Result<Void> result = Result.success();
        assertEquals(SystemErrorCode.SUCCESS.getCode(), result.getCode());
        assertNull(result.getData());
    }

    @Test
    void successWithDataAndMessage() {
        Result<String> result = Result.success("ok", "done");
        assertEquals("ok", result.getData());
        assertEquals("done", result.getMsg());
    }

    @Test
    void failedVariants() {
        assertEquals(SystemErrorCode.FAILED.getCode(), Result.failed().getCode());
        assertEquals("bad", Result.failed("bad").getMsg());
        assertEquals(SystemErrorCode.VALIDATE_FAILED.getCode(), Result.validateFailed().getCode());
        assertEquals(SystemErrorCode.UNAUTHORIZED.getCode(), Result.unauthorized().getCode());
        assertEquals(SystemErrorCode.FORBIDDEN.getCode(), Result.forbidden("x").getCode());
    }

    @Test
    void settersAndGetters() {
        Result<Integer> result = new Result<>(1, "m", 2);
        result.setCode(3);
        result.setMessage("msg");
        result.setData(4);
        assertEquals(3, result.getCode());
        assertEquals("msg", result.getMsg());
        assertEquals(4, result.getData());
    }
}
