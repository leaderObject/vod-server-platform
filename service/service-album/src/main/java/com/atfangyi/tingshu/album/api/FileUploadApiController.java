package com.atfangyi.tingshu.album.api;

import com.atfangyi.tingshu.album.service.FileUploadService;
import com.atfangyi.tingshu.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "上传管理接口")
@RestController
@RequestMapping("api/album")
public class FileUploadApiController {

    @Autowired
    private FileUploadService fileUploadService;

    @Operation(summary = "文件上传")
    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file) {
        return Result.ok(fileUploadService.fileUpload(file));
    }


}
