package com.atfangyi.tingshu.user.strategy.Impl;

/*
 * @Author:  方毅
 * @date:  2025/11/5 21:35
 */

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import com.atfangyi.tingshu.common.constant.SystemConstant;
import com.atfangyi.tingshu.common.execption.GuiguException;
import com.atfangyi.tingshu.common.result.ResultCodeEnum;
import com.atfangyi.tingshu.model.user.UserInfo;
import com.atfangyi.tingshu.user.mapper.UserInfoMapper;
import com.atfangyi.tingshu.user.mapper.UserPaidAlbumMapper;
import com.atfangyi.tingshu.user.strategy.UserPaidStrategy;
import com.atfangyi.tingshu.vo.user.UserPaidRecordVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component(SystemConstant.ORDER_ITEM_TYPE_ALBUM)
public class UserPaidAlbum implements UserPaidStrategy {

    @Autowired
    private UserPaidAlbumMapper userPaidAlbumMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserPaid(UserPaidRecordVo userPaidRecordVo) {
        UserInfo userInfo = userInfoMapper.selectById(userPaidRecordVo.getUserId());
        Assert.notNull(userInfo, "数据出现异常userInfo");
        com.atfangyi.tingshu.model.user.UserPaidAlbum userPaidAlbum = new com.atfangyi.tingshu.model.user.UserPaidAlbum();
        userPaidAlbum.setUserId(userPaidAlbum.getUserId());
        userPaidAlbum.setOrderNo(userPaidRecordVo.getOrderNo());
        if (CollectionUtil.isNotEmpty(userPaidRecordVo.getItemIdList()))
            userPaidAlbum.setAlbumId(userPaidRecordVo.getItemIdList().get(0));
        if (userPaidAlbumMapper.insert(userPaidAlbum) < 1) throw new GuiguException(ResultCodeEnum.FAIL);
    }
}
