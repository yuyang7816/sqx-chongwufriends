package com.sqx.common.web.config;

import com.sqx.common.core.utils.LocalUploadPathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地上传目录静态映射，保证 /upload/** 可通过服务访问。
 */
@Configuration
public class LocalUploadResourceConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(LocalUploadResourceConfig.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        List<Path> roots = LocalUploadPathHelper.resolveUploadRootsForServing();
        List<String> locations = new ArrayList<>();
        for (Path root : roots) {
            String location = root.toUri().toString();
            if (!location.endsWith("/")) {
                location = location + "/";
            }
            locations.add(location);
        }
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(locations.toArray(new String[0]));
        log.info("本地上传静态目录映射: /upload/** -> {}", locations);
    }
}
