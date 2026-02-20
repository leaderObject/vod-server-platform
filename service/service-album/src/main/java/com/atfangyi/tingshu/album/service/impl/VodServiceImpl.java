package com.atfangyi.tingshu.album.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.album.config.VodConstantProperties;
import com.atfangyi.tingshu.album.service.VodService;
import com.atfangyi.tingshu.common.util.UploadFileUtil;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.DescribeMediaInfosRequest;
import com.tencentcloudapi.vod.v20180717.models.DescribeMediaInfosResponse;
import com.tencentcloudapi.vod.v20180717.models.MediaInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class VodServiceImpl implements VodService {

    @Autowired
    private VodConstantProperties vodConstantProperties;

    @Autowired
    private VodUploadClient vodUploadClient;


    @Override
    public Map<String, Object> uploadTrack(MultipartFile file) {
        VodUploadRequest request = new VodUploadRequest();
        String tempPath = UploadFileUtil.uploadTempPath(vodConstantProperties.getTempPath(), file);
        request.setMediaFilePath(tempPath);
        request.setProcedure(vodConstantProperties.getProcedure());
        try {
            VodUploadResponse response = vodUploadClient.upload("ap-guangzhou", request);
            HashMap<String, Object> map = new HashMap<>();
            map.put("mediaFileId", response.getFileId());
            map.put("mediaUrl", response.getMediaUrl());
            log.info("[专辑服务]上传音频文件到点播平台成功", response.getFileId());
            return map;
        } catch (Exception e) {
            // 业务方进行异常处理
            log.error("[专辑服务]上传音频文件到点播平台异常：文件：{}，错误信息：{}", file, e);
        }
        return null;
    }

    @Override
    public JSONObject getMediaDetailInfo(String mediaFileId) {
        try {
            Credential cred = new Credential(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());
            // 使用临时密钥示例
            // Credential cred = new Credential("SecretId", "SecretKey", "Token");
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("vod.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            VodClient client = new VodClient(cred, vodConstantProperties.getRegion(), clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeMediaInfosRequest req = new DescribeMediaInfosRequest();
            req.setFileIds(new String[]{mediaFileId});
            // 返回的resp是一个DescribeMediaInfosResponse的实例，与请求对象对应
            DescribeMediaInfosResponse resp = client.DescribeMediaInfos(req);
            MediaInfo mediaInfo = resp.getMediaInfoSet()[0];
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(mediaInfo));
            log.info("[专辑服务]查询音频文件详情成功：{}", jsonObject);
            return jsonObject;
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
        return null;
    }
}
