package com.atfangyi.tingshu.album.service.impl;

/*
 * @Author:  方毅
 * @date:  2025/10/13 16:49
 */

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.atfangyi.tingshu.album.config.MinioConstantProperties;
import com.atfangyi.tingshu.album.service.FileUploadService;
import com.atfangyi.tingshu.common.execption.GuiguException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {
    /**
     * 文件上传 返回响应地址
     *
     * @param file
     * @return
     */

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConstantProperties minioConstantProperties;


    @Override
    public String fileUpload(MultipartFile file) {
        BufferedImage read = null;
        try {
            read = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (read == null) {
            throw new GuiguException(400, "图片格式非法！");
        }
        String FoldName = "/" + DateUtil.today() + "/";
        String FileType = FileUtil.extName(file.getOriginalFilename());
        String FileName = IdUtil.randomUUID() + "." + FileType;
        String filePath = FoldName + FileName;
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(minioConstantProperties.getBucketName())
                            .object(filePath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );
        } catch (Exception e) {
            log.error("数据传输出错{}", e.getMessage());
            throw new RuntimeException(e);
        }
        return minioConstantProperties.getEndpointUrl() + "/" + minioConstantProperties.getBucketName() + filePath;

    }
}
