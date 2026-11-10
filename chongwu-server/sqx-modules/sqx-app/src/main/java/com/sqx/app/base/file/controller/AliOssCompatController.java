package com.sqx.app.base.file.controller;

import com.sqx.app.base.file.service.FileService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.file.domain.UploadRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 兼容历史前端 alioss 上传路径，内部复用统一文件上传。
 */
@Tag(name = "文件-兼容", description = "alioss 兼容上传")
@RestController
@RequestMapping("alioss")
public class AliOssCompatController {

    @Resource
    private FileService fileService;

    @Operation(summary = "兼容上传 alioss/upload")
    @PostMapping("upload")
    public Result<UploadRes> upload(MultipartFile file, HttpServletRequest request) {
        Result<UploadRes> result = fileService.upload(file);
        if (result != null && result.getData() != null) {
            FileController.fillAccessiblePath(result.getData(), request);
        }
        return result;
    }

    @Operation(summary = "兼容上传 alioss/uploads")
    @PostMapping("uploads")
    public Result<UploadRes> uploads(MultipartFile file, HttpServletRequest request) {
        return upload(file, request);
    }
}
