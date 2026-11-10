package com.sqx.common.file.storage;

import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.LocalUploadPathHelper;
import com.sqx.common.file.domain.UploadParam;
import com.sqx.common.file.domain.UploadRes;
import com.sqx.common.file.enums.FileError;
import com.sqx.common.file.utils.FileUtil;
import com.sqx.common.redis.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author chengming
 * @since 2024/10/25
 */
@Slf4j
public class LocalFileStorage implements IFileStorage {

    /**
     * 简易的文件上传
     * @param file 文件
     */
    @Override
    public UploadRes upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BizException(FileError.NONSUPPORT_EMPTY_FILE);
        }

        String uploadPath = ConfigUtil.getConfigValue("file_local_upload_path");
        if (StringUtils.isBlank(uploadPath)) {
            uploadPath = "/upload";
        }
        if (!uploadPath.startsWith("/")) {
            uploadPath = "/" + uploadPath;
        }

        try {
            Path uploadRoot = LocalUploadPathHelper.resolvePrimaryUploadRoot();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            String dataFolder = LocalDateTime.now().format(formatter);
            Path uploadFolderPath = uploadRoot.resolve(dataFolder);

            // 确保目标目录存在
            if (!Files.exists(uploadFolderPath)) {
                Files.createDirectories(uploadFolderPath);
            }

            // 重命名文件
            String fileExtension = FileUtil.getFileExtension(file.getOriginalFilename());
            String newName = UUID.randomUUID() + "." + fileExtension;

            Path path = uploadFolderPath.resolve(newName);
            file.transferTo(path.toFile());

            // 返回上传文件的访问路径（相对 /upload/...）
            String filePath = uploadPath + "/" + dataFolder + "/" + newName;

            UploadRes uploadRes = new UploadRes();
            uploadRes.setRelativePath(filePath);
            uploadRes.setExtension(fileExtension);
            uploadRes.setSize(file.getSize());
            // 本地文件通过当前服务 /upload/** 访问；path 先回相对路径，前端再拼 API 域名
            // 若配置了真实可访问的 CDN（且不是空壳业务域名），仍拼绝对地址
            uploadRes.setPath(buildPublicUrl(filePath));
            return uploadRes;
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new BizException(FileError.UPLOAD_FAILED);
        }
    }

    @Override
    public UploadRes upload(UploadParam uploadParam) {
        return null;
    }

    /**
     * 配置了 file_cdn_domain 时直接拼绝对地址；否则回相对路径，由 FileController 按请求 Host 补全。
     */
    private String buildPublicUrl(String filePath) {
        String normalizedPath = filePath == null ? "" : filePath.trim();
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        String cdn = ConfigUtil.getConfigValueOrDefault("file_cdn_domain");
        if (StringUtils.isNotBlank(cdn) && looksLikeHttpUrl(cdn) && !isLoopbackUrl(cdn)) {
            return cdn.trim().replaceAll("/+$", "") + normalizedPath;
        }
        return normalizedPath;
    }

    private boolean looksLikeHttpUrl(String domain) {
        String value = domain.trim().toLowerCase();
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private boolean isLoopbackUrl(String domain) {
        String value = domain.trim().toLowerCase();
        return value.contains("://localhost")
                || value.contains("://127.0.0.1")
                || value.contains("://0.0.0.0");
    }
}
