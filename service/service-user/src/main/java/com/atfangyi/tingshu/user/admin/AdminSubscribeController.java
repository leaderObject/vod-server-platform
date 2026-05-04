package com.atfangyi.tingshu.user.admin;

import com.atfangyi.tingshu.album.AlbumFeignClient;
import com.atfangyi.tingshu.common.annotation.AdminLogin;
import com.atfangyi.tingshu.common.annotation.OperatorLogAnnotation;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.common.util.MongoUtil;
import com.atfangyi.tingshu.model.album.AlbumInfo;
import com.atfangyi.tingshu.model.user.UserSubscribe;
import com.atfangyi.tingshu.user.service.UserInfoService;
import com.atfangyi.tingshu.vo.user.UserSubscribeAdminVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "订阅管理")
@RestController
@RequestMapping("/admin/subscribe")
@SuppressWarnings({"all"})
public class AdminSubscribeController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private AlbumFeignClient albumFeignClient;

    @Operation(summary = "分页查询订阅列表")
    @GetMapping("/getSubscribePage/{page}/{limit}")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4", operatorMethod = "getSubscribePage")
    public Result<IPage<UserSubscribeAdminVo>> getSubscribePage(
            @PathVariable Long page,
            @PathVariable Long limit,
            @RequestParam(required = false) Long albumId,
            @RequestParam(required = false) Long userId) {

        List<UserSubscribeAdminVo> allSubscribes = new ArrayList<>();

        // 查询所有用户的订阅记录（需要遍历可能的分区）
        // 由于MongoDB使用分表，这里简化处理，实际可以根据albumId或userId精确定位

        // 简化：直接查询所有分表
        for (int i = 0; i < 100; i++) {
            String collectionName = "userSubscribe_" + i;
            Query query = new Query();

            if (albumId != null) {
                query.addCriteria(Criteria.where("albumId").is(albumId));
            }
            if (userId != null) {
                query.addCriteria(Criteria.where("userId").is(userId));
            }

            query.with(Sort.by(Sort.Direction.DESC, "createTime"));

            try {
                List<UserSubscribe> subscribes = mongoTemplate.find(query, UserSubscribe.class, collectionName);
                for (UserSubscribe subscribe : subscribes) {
                    UserSubscribeAdminVo vo = new UserSubscribeAdminVo();
                    vo.setId(subscribe.getId());
                    vo.setUserId(subscribe.getUserId());
                    vo.setAlbumId(subscribe.getAlbumId());
                    vo.setCreateTime(subscribe.getCreateTime());

                    // 获取用户信息
                    com.atfangyi.tingshu.model.user.UserInfo userInfo = userInfoService.queryUserInfoById(subscribe.getUserId());
                    if (userInfo != null) {
                        vo.setNickname(userInfo.getNickname());
                        vo.setAvatarUrl(userInfo.getAvatarUrl());
                    }

                    // 获取专辑信息
                    try {
                        Result<AlbumInfo> albumResult = albumFeignClient.getAlbumInfoById(subscribe.getAlbumId());
                        if (albumResult != null && albumResult.getData() != null) {
                            vo.setAlbumTitle(albumResult.getData().getAlbumTitle());
                            vo.setCoverUrl(albumResult.getData().getCoverUrl());
                        }
                    } catch (Exception e) {
                        // 获取专辑信息失败，使用默认值
                        vo.setAlbumTitle("专辑" + subscribe.getAlbumId());
                    }

                    allSubscribes.add(vo);
                }
            } catch (Exception e) {
                // 分表不存在，跳过
            }
        }

        // 按时间排序
        allSubscribes.sort((a, b) -> {
            if (a.getCreateTime() == null && b.getCreateTime() == null) return 0;
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        // 分页
        int start = (int) ((page - 1) * limit);
        int end = (int) Math.min(start + limit, allSubscribes.size());

        List<UserSubscribeAdminVo> pageList = start < allSubscribes.size() ? allSubscribes.subList(start, end) : new ArrayList<>();

        Page<UserSubscribeAdminVo> pageResult = new Page<>(page, limit, allSubscribes.size());
        pageResult.setRecords(pageList);

        return Result.ok(pageResult);
    }

    @Operation(summary = "获取订阅总数")
    @GetMapping("/getSubscribeCount")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4", operatorMethod = "getSubscribeCount")
    public Result<Long> getSubscribeCount(
            @RequestParam(required = false) Long albumId,
            @RequestParam(required = false) Long userId) {

        long totalCount = 0;

        for (int i = 0; i < 100; i++) {
            String collectionName = "userSubscribe_" + i;
            Query query = new Query();

            if (albumId != null) {
                query.addCriteria(Criteria.where("albumId").is(albumId));
            }
            if (userId != null) {
                query.addCriteria(Criteria.where("userId").is(userId));
            }

            try {
                totalCount += mongoTemplate.count(query, collectionName);
            } catch (Exception e) {
                // 分表不存在，跳过
            }
        }

        return Result.ok(totalCount);
    }

    @Operation(summary = "根据专辑ID获取订阅用户列表")
    @GetMapping("/getSubscribersByAlbum/{albumId}/{page}/{limit}")
    @AdminLogin
    @OperatorLogAnnotation(operatorType = "4", operatorMethod = "getSubscribersByAlbum")
    public Result<IPage<UserSubscribeAdminVo>> getSubscribersByAlbum(
            @PathVariable Long albumId,
            @PathVariable Long page,
            @PathVariable Long limit) {

        return getSubscribePage(page, limit, albumId, null);
    }
}
