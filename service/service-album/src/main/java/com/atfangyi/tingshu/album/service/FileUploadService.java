package com.atfangyi.tingshu.album.service;

import org.springframework.web.multipart.MultipartFile;

/*
 * @Author:  方毅
 * @date:  2025/10/13 16:49
 */
public interface FileUploadService {
    String fileUpload(MultipartFile file);

}
