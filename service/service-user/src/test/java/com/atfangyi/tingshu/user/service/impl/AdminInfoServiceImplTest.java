package com.atfangyi.tingshu.user.service.impl;


import com.atfangyi.tingshu.ServiceUserApplication;
import com.atfangyi.tingshu.common.util.MD5;
import com.atfangyi.tingshu.model.user.AdminInfo;
import com.atfangyi.tingshu.user.service.AdminInfoService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @className: AdminInfoServiceImplTest
 * @author: 方毅
 * @date: 2025/12/11 13:39
 * @version: 1.0
 * @description: TODO
 */

@SuppressWarnings({"all"})
@SpringBootTest(classes = ServiceUserApplication.class)
class AdminInfoServiceImplTest {

    @Resource
    private AdminInfoService adminInfoService;


    @Test
    public void data() {
//        AdminInfo adminInfo = new AdminInfo();
//        adminInfo.setAvatarUrl("https://glsx.oss-cn-beijing.aliyuncs.com/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_2025-12-10_150742_410.jpg");
//        adminInfo.setGender(1);
//        adminInfo.setPhone("15502580452");
//        adminInfo.setUsername("fangyi");
//        adminInfo.setIntroduce("菜鸟程序员一枚");
//        adminInfoService.save(adminInfo);
//    }
        System.out.println(MD5.encrypt("fangyi"));

        }
    }

