package com.atfangyi.tingshu.user.service.impl;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.atfangyi.tingshu.common.ParamAssert.ServiceAssert;
import com.atfangyi.tingshu.common.annotation.AdminLogin;
import com.atfangyi.tingshu.common.annotation.Cache;
import com.atfangyi.tingshu.common.constant.RedisConstant;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.common.util.*;
import com.atfangyi.tingshu.dto.AdminDto;
import com.atfangyi.tingshu.dto.PhoneLoginDto;
import com.atfangyi.tingshu.model.user.AdminInfo;

import com.atfangyi.tingshu.user.annotation.SendCode;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atfangyi.tingshu.user.service.AdminInfoService;
import com.atfangyi.tingshu.user.mapper.AdminInfoMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @description 针对表【admin_info(管理员信息)】的数据库操作Service实现
 * @createDate 2025-12-10 17:43:47
 */
@Service
@Slf4j
public class AdminInfoServiceImpl extends ServiceImpl<AdminInfoMapper, AdminInfo>
        implements AdminInfoService {


    private static final String APP_CODE = "6a20ee185da9496ca2b6125462ddbbab";

    @Resource
    private AdminInfoMapper adminInfoMapper;

    @Resource
    private RedisTemplate redisTemplate;


    @Override
    public String login(AdminDto adminDto, HttpServletRequest serverHttpRequest) {
        //判断用户是否存在
        AdminInfo adminInfo = null;
        try {
            adminInfo = adminInfoMapper.selectOne(Wrappers.lambdaQuery(AdminInfo.class).eq(AdminInfo::getUsername, adminDto.getUsername()));
        } catch (Exception e) {
            log.error("用户存在多个  联系管理员处理 {}", e.getMessage());
            throw new GuiguException(ResultCodeEnum.FAIL);
        }
        //判断用户是否存在
        if (adminInfo == null) throw new GuiguException(ResultCodeEnum.ACCOUNT_ERROR);
        //判断密码是否正确
        if (!adminInfo.getPassword().equals(MD5.encrypt(adminDto.getPassword())))
            throw new GuiguException(ResultCodeEnum.PASSWORD_ERROR);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", adminInfo.getId());
        jsonObject.put("username", adminInfo.getUsername());
        this.initAddress(adminInfo.getId(), serverHttpRequest);
        adminInfo = adminInfoMapper.selectOne(Wrappers.lambdaQuery(AdminInfo.class).eq(AdminInfo::getUsername, adminDto.getUsername()));
        redisTemplate.opsForValue().set(RedisConstant.ADMIN_INFO_PREFIX + adminInfo.getId(), adminInfo);
        String jwt = JwtUtil.createJWT(jsonObject.toString());
        return jwt;

    }

    public static void main(String[] args) {
        System.out.println(MD5.encrypt("fangyi"));
    }
    /**
     * 发送验证码业务
     *
     * @param phone
     * @return
     */
    @Override
    @SendCode
    public boolean code(String phone) {
        //查询验证码发送的次数 限制每天发送三次
        JSONObject jsonObject = SendCodeTool(phone);
        log.info("发送验证码响应: {}", jsonObject);
        if (jsonObject == null) {
            log.error("短信发送服务异常，请检查短信API配置或网络连接");
            throw new GuiguException(ResultCodeEnum.FAIL.getCode(), "短信发送服务暂时不可用，请稍后重试或联系管理员");
        }
        if (jsonObject.get("code").equals("0")) {
            redisTemplate.opsForValue().set(phone + ":code", jsonObject.get("codemsg"), 3, TimeUnit.MINUTES);
        }
        return jsonObject.get("code").equals("0");
    }

    /**
     * 手机号登录
     *
     * @param phoneLoginDto
     * @return
     */
    @Override
    public String phoneLogin(PhoneLoginDto phoneLoginDto, HttpServletRequest serverHttpRequest) {
        Assert.notNull(phoneLoginDto);
        String phone = phoneLoginDto.getPhone();
        Object object = redisTemplate.opsForValue().get(phone + ":code");
        //未发送验证码
        if (object == null) throw new GuiguException(501, "请先发送验证码");
        //验证码错误
        if (!phoneLoginDto.getCode().equals(object)) throw new GuiguException(ResultCodeEnum.PHONE_CODE_ERROR);
        //查询手机好是否绑定
        AdminInfo adminInfo = adminInfoMapper.selectOne(Wrappers.lambdaQuery(AdminInfo.class).eq(AdminInfo::getPhone, phone));
        if (adminInfo == null) {
            //当前账号为专属账号 必须绑定
            log.info("请联系管理员方毅");
            throw new GuiguException(ResultCodeEnum.PHONE_LOGIN_ERROR);
        }

        //返回token
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", adminInfo.getId());
        jsonObject.put("username", adminInfo.getUsername());
        this.initAddress(adminInfo.getId(), serverHttpRequest);
        adminInfo = adminInfoMapper.selectOne(Wrappers.lambdaQuery(AdminInfo.class).eq(AdminInfo::getPhone, phone));
        redisTemplate.opsForValue().set(RedisConstant.ADMIN_INFO_PREFIX + adminInfo.getId(), adminInfo);
        return JwtUtil.createJWT(jsonObject.toString());
    }



    public void initAddress(Long id, HttpServletRequest serverHttpRequest) {
        LambdaUpdateWrapper<AdminInfo> adminInfoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        adminInfoLambdaUpdateWrapper.set(AdminInfo::getIpAddress, IpUtil.getIpAddress(serverHttpRequest));
        adminInfoLambdaUpdateWrapper.eq(AdminInfo::getId, id);
        adminInfoMapper.update(null, adminInfoLambdaUpdateWrapper);

    }


    @Override
    @Cache(prefix = "adminInfo:#id")
    public AdminInfo queryUserInfoById(Long id) {
        AdminInfo adminInfo = adminInfoMapper.selectById(id);
        ServiceAssert.ObjectAssert(adminInfo);
        return adminInfo;
    }

    protected static String GetCodeToll() {
        return new Random().nextInt(100000, 999999) + "";
    }

    protected static JSONObject SendCodeTool(String phone) {
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + APP_CODE);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        String codemsg = GetCodeToll();
        querys.put("param", "**code**:" + codemsg + ",**minute**:3");
        //smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html
        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<String, String>();
        try {
            HttpResponse httpResponse = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            log.info("短信发送响应状态: {}", httpResponse.getStatusLine());
            //获取response的body
            String responseStr = EntityUtils.toString(httpResponse.getEntity());
            log.info("短信发送响应内容: {}", responseStr);
            if (responseStr == null || responseStr.isEmpty()) {
                log.error("短信发送失败: 响应体为空");
                return null;
            }
            JSONObject jsonObject = JSONObject.parseObject(responseStr);
            if (jsonObject == null) {
                log.error("短信发送失败: JSON解析结果为空");
                return null;
            }
            jsonObject.put("codemsg", codemsg);
            return jsonObject;
        } catch (Exception e) {
            log.error("短信发送异常: {}", e.getMessage(), e);
        }
        return null;
    }
}




