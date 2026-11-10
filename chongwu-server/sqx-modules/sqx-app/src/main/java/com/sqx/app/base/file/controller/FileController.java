package com.sqx.app.base.file.controller;

import com.sqx.app.base.file.service.FileService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.file.domain.UploadRes;
import com.sqx.common.redis.utils.ConfigUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author chengming
 * @since 2025/4/29
 */
@Tag(name = "文件", description = "文件")
@RestController
@RequestMapping("file")
public class FileController {
    /** 管理端文件访问兜底域名（请求主机为 localhost 时使用） */
    private static final String DEFAULT_ADMIN_FILE_BASE = "https://cwjpyadmin.xianmxkj.com/sqx_fast";

    @Resource
    private FileService fileService;

    @Operation(summary = "普通文件上传")
    @PostMapping("upload")
    public Result<UploadRes> upload(MultipartFile file, HttpServletRequest request) {
        Result<UploadRes> result = fileService.upload(file);
        if (result != null && result.getData() != null) {
            fillAccessiblePath(result.getData(), request);
        }
        return result;
    }

    /**
     * 将本地 upload 相对路径补全为可公网访问的绝对地址。
     * 优先保留已是线上域名的 path；否则按请求 Host / 配置域名拼接，避免 Nginx 反代后变成 localhost。
     */
    static void fillAccessiblePath(UploadRes data, HttpServletRequest request) {
        if (data == null) {
            return;
        }
        String relative = StringUtils.trimToEmpty(data.getRelativePath());
        if (StringUtils.isBlank(relative)) {
            relative = extractUploadRelative(data.getPath());
        }
        if (StringUtils.isBlank(relative)) {
            return;
        }
        if (!relative.startsWith("/")) {
            relative = "/" + relative;
        }
        if (!relative.startsWith("/upload/")) {
            return;
        }

        String path = StringUtils.trimToEmpty(data.getPath());
        if (isPublicAbsoluteUploadUrl(path)) {
            // 已是可公网访问地址，仅做 localhost 纠偏
            return;
        }

        String base = resolvePublicBase(request);
        data.setPath(base.replaceAll("/+$", "") + relative);
    }

    static String resolvePublicBase(HttpServletRequest request) {
        String cdn = StringUtils.trimToEmpty(ConfigUtil.getConfigValueOrDefault("file_cdn_domain"));
        if (StringUtils.isNotBlank(cdn) && looksLikeHttpUrl(cdn) && !isLoopbackUrl(cdn)) {
            return cdn.replaceAll("/+$", "");
        }

        if (request != null) {
            String host = firstHeaderValue(request.getHeader("X-Forwarded-Host"));
            if (StringUtils.isBlank(host)) {
                host = firstHeaderValue(request.getHeader("Host"));
            }
            if (StringUtils.isBlank(host)) {
                host = request.getServerName();
                int port = request.getServerPort();
                String schemeProbe = firstHeaderValue(request.getHeader("X-Forwarded-Proto"));
                if (StringUtils.isBlank(schemeProbe)) {
                    schemeProbe = request.getScheme();
                }
                if (!isDefaultPort(schemeProbe, port)) {
                    host = host + ":" + port;
                }
            }

            if (StringUtils.isNotBlank(host) && !isLoopbackHost(host)) {
                String scheme = firstHeaderValue(request.getHeader("X-Forwarded-Proto"));
                if (StringUtils.isBlank(scheme)) {
                    scheme = StringUtils.defaultIfBlank(request.getScheme(), "https");
                }
                if (host.startsWith("http://") || host.startsWith("https://")) {
                    return host.replaceAll("/+$", "") + StringUtils.defaultString(request.getContextPath());
                }
                return scheme + "://" + host + StringUtils.defaultString(request.getContextPath());
            }
        }

        if (StringUtils.isNotBlank(cdn) && looksLikeHttpUrl(cdn)) {
            return cdn.replaceAll("/+$", "");
        }
        return DEFAULT_ADMIN_FILE_BASE;
    }

    private static boolean isPublicAbsoluteUploadUrl(String path) {
        if (StringUtils.isBlank(path) || path.startsWith("/")) {
            return false;
        }
        if (!looksLikeHttpUrl(path) || !path.contains("/upload/")) {
            return false;
        }
        return !isLoopbackUrl(path);
    }

    private static String extractUploadRelative(String path) {
        String value = StringUtils.trimToEmpty(path);
        if (StringUtils.isBlank(value)) {
            return "";
        }
        int idx = value.indexOf("/upload/");
        if (idx >= 0) {
            return value.substring(idx);
        }
        return "";
    }

    private static boolean looksLikeHttpUrl(String value) {
        String v = value.trim().toLowerCase();
        return v.startsWith("http://") || v.startsWith("https://");
    }

    private static boolean isLoopbackUrl(String value) {
        String v = value.trim().toLowerCase();
        return v.contains("://localhost") || v.contains("://127.0.0.1") || v.contains("://0.0.0.0");
    }

    private static boolean isLoopbackHost(String host) {
        String h = host.trim().toLowerCase();
        if (h.contains(",")) {
            h = h.split(",")[0].trim();
        }
        if (h.contains("/")) {
            h = h.substring(0, h.indexOf('/'));
        }
        return h.startsWith("localhost") || h.startsWith("127.0.0.1") || h.startsWith("0.0.0.0");
    }

    private static String firstHeaderValue(String raw) {
        if (StringUtils.isBlank(raw)) {
            return "";
        }
        String value = raw.trim();
        int comma = value.indexOf(',');
        if (comma > 0) {
            value = value.substring(0, comma).trim();
        }
        return value;
    }

    private static boolean isDefaultPort(String scheme, int port) {
        return ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
    }
}
