package com.sqx.common.core.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 统一解析本地 upload 根目录，避免 IDEA 工作目录在「项目根 / sqx-application」之间切换导致上传与静态映射不一致。
 */
public final class LocalUploadPathHelper {

    private LocalUploadPathHelper() {
    }

    /**
     * 写入新文件时使用的主目录（不存在则创建）。
     */
    public static Path resolvePrimaryUploadRoot() {
        Path preferred = preferExistingOrCreatable();
        try {
            Files.createDirectories(preferred);
        } catch (Exception ignored) {
            // 创建失败时仍返回该路径，由调用方抛出业务异常
        }
        return preferred.toAbsolutePath().normalize();
    }

    /**
     * 静态资源映射候选目录（含历史目录），用于兼容已上传文件。
     */
    public static List<Path> resolveUploadRootsForServing() {
        Set<Path> roots = new LinkedHashSet<>();
        roots.add(resolvePrimaryUploadRoot());
        for (Path candidate : candidateRoots()) {
            if (Files.isDirectory(candidate)) {
                roots.add(candidate.toAbsolutePath().normalize());
            }
        }
        return new ArrayList<>(roots);
    }

    private static Path preferExistingOrCreatable() {
        List<Path> candidates = candidateRoots();
        // 1) 已有内容的目录优先（避免空壳目录抢占）
        Path bestExisting = null;
        long bestCount = -1;
        for (Path candidate : candidates) {
            if (!Files.isDirectory(candidate)) {
                continue;
            }
            long count = countFilesQuietly(candidate);
            if (count > bestCount) {
                bestCount = count;
                bestExisting = candidate;
            }
        }
        if (bestExisting != null && bestCount > 0) {
            return bestExisting;
        }
        // 2) 已存在的空目录
        if (bestExisting != null) {
            return bestExisting;
        }
        // 3) 按优先级创建：sqx-application > 项目根
        for (Path candidate : candidates) {
            Path parent = candidate.getParent();
            if (parent != null && Files.isDirectory(parent)) {
                return candidate;
            }
        }
        return candidates.get(0);
    }

    private static List<Path> candidateRoots() {
        Path userDir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        List<Path> list = new ArrayList<>();
        // IDEA 工作目录常为仓库根
        list.add(userDir.resolve("chongwu-server").resolve("sqx-application").resolve("upload"));
        list.add(userDir.resolve("sqx-application").resolve("upload"));
        list.add(userDir.resolve("upload"));
        // 工作目录已是 sqx-application 时
        if (userDir.getFileName() != null && "sqx-application".equalsIgnoreCase(userDir.getFileName().toString())) {
            list.add(0, userDir.resolve("upload"));
        }
        return list;
    }

    private static long countFilesQuietly(Path root) {
        try {
            return Files.walk(root).filter(Files::isRegularFile).limit(50).count();
        } catch (Exception e) {
            return 0;
        }
    }
}
