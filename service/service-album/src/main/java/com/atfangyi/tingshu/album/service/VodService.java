package com.atfangyi.tingshu.album.service;

import com.alibaba.fastjson.JSONObject;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface VodService {

    Map<String, Object> uploadTrack(MultipartFile file);

    JSONObject getMediaDetailInfo(@NotEmpty(message = "媒体文件Id不能为空") String mediaFileId);

}
