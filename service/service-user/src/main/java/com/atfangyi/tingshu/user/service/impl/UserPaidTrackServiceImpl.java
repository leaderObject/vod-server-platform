package com.atfangyi.tingshu.user.service.impl;

import com.atfangyi.tingshu.model.user.UserPaidTrack;
import com.atfangyi.tingshu.user.mapper.UserPaidAlbumMapper;
import com.atfangyi.tingshu.user.mapper.UserPaidTrackMapper;
import com.atfangyi.tingshu.user.service.UserPaidTrackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({"all"})
public class UserPaidTrackServiceImpl extends ServiceImpl<UserPaidTrackMapper, UserPaidTrack> implements UserPaidTrackService {

	@Autowired
	private UserPaidAlbumMapper userPaidAlbumMapper;

}
