package com.atfangyi.tingshu.album.service.impl;

import cn.hutool.core.util.IdUtil;
import com.atfangyi.tingshu.album.service.CommentService;
import com.atfangyi.tingshu.common.util.AuthContextHolder;
import com.atfangyi.tingshu.common.util.MongoUtil;
import com.atfangyi.tingshu.model.comment.Comment;
import com.atfangyi.tingshu.model.comment.CommentPraise;
import com.atfangyi.tingshu.model.user.UserInfo;
import com.atfangyi.tingshu.user.client.UserFeignClient;
import com.atfangyi.tingshu.vo.comment.CommentVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class CommentServiceImpl implements CommentService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private UserFeignClient userFeignClient;

    @Override
    public IPage<CommentVo> findCommentPage(Long albumId, Long trackId, Long page, Long limit) {
        List<CommentVo> voList = new ArrayList<>();
        long total = 0;

        if (albumId != null) {
            String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.COMMENT, albumId);

            Query query = new Query();
            query.addCriteria(Criteria.where("albumId").is(albumId));
            if (trackId != null && trackId > 0) {
                query.addCriteria(Criteria.where("trackId").is(trackId));
            }
            query.addCriteria(Criteria.where("replyCommentId").isNull());
            query.with(Sort.by(Sort.Direction.DESC, "createTime"));

            Long userId = AuthContextHolder.getUserId();
            total = mongoTemplate.count(query, collectionName);

            Pageable pageable = PageRequest.of(page.intValue() - 1, limit.intValue());
            query.with(pageable);
            List<Comment> commentList = mongoTemplate.find(query, Comment.class, collectionName);

            for (Comment comment : commentList) {
                CommentVo vo = new CommentVo();
                BeanUtils.copyProperties(comment, vo);
                vo.setAlbumId(comment.getAlbumId());

                if (userId != null && userId > 0) {
                    vo.setIsPraise(checkIsPraised(userId, comment.getId()));
                }

                Query replyQuery = new Query(Criteria.where("replyCommentId").is(comment.getId()));
                replyQuery.with(Sort.by(Sort.Direction.ASC, "createTime"));
                List<Comment> replyList = mongoTemplate.find(replyQuery, Comment.class, collectionName);
                if (replyList != null && !replyList.isEmpty()) {
                    List<CommentVo> replyVoList = new ArrayList<>();
                    for (Comment reply : replyList) {
                        CommentVo replyVo = new CommentVo();
                        BeanUtils.copyProperties(reply, replyVo);
                        replyVoList.add(replyVo);
                    }
                    vo.setReplyCommentList(replyVoList);
                }

                voList.add(vo);
            }
        }

        Page<CommentVo> pageResult = new Page<>(page, limit, total);
        pageResult.setRecords(voList);
        return pageResult;
    }

    @Override
    public boolean saveComment(Comment comment) {
        try {
            Long userId = AuthContextHolder.getUserId();
            if (userId == null) {
                log.error("保存评论失败：用户未登录");
                return false;
            }

            // 通过Feign获取用户信息
            UserInfo userInfo = userFeignClient.queryUserInfoByUserId(userId);
            if (userInfo != null) {

                comment.setNickname(userInfo.getNickname());
                comment.setAvatarUrl(userInfo.getAvatarUrl());
            }

            comment.setId(IdUtil.getSnowflakeNextIdStr());
            comment.setUserId(userId);
            comment.setPraiseCount(0);
            comment.setCreateTime(new Date());

            String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.COMMENT, comment.getAlbumId());
            mongoTemplate.save(comment, collectionName);
            return true;
        } catch (Exception e) {
            log.error("保存评论失败", e);
            return false;
        }
    }

    @Override
    public boolean praiseComment(Long albumId, String commentId) {
        try {
            Long userId = AuthContextHolder.getUserId();
            if (userId == null) {
                return false;
            }

            String praiseCollectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.COMMENT_PRAISE, userId);
            Query praiseQuery = new Query(Criteria.where("commentId").is(commentId));
            CommentPraise existingPraise = mongoTemplate.findOne(praiseQuery, CommentPraise.class, praiseCollectionName);

            String commentCollectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.COMMENT, albumId);
            Query commentQuery = new Query(Criteria.where("id").is(commentId));

            if (existingPraise != null) {
                mongoTemplate.remove(praiseQuery, CommentPraise.class, praiseCollectionName);
                mongoTemplate.updateFirst(commentQuery, new Update().inc("praiseCount", -1), commentCollectionName);
                return true;
            } else {
                CommentPraise praise = new CommentPraise();
                praise.setId(IdUtil.getSnowflakeNextIdStr());
                praise.setUserId(userId);
                praise.setCommentId(commentId);
                praise.setAlbumId(albumId);
                praise.setCreateTime(new Date());
                mongoTemplate.save(praise, praiseCollectionName);
                mongoTemplate.updateFirst(commentQuery, new Update().inc("praiseCount", 1), commentCollectionName);
                return true;
            }
        } catch (Exception e) {
            log.error("点赞评论失败", e);
            return false;
        }
    }

    @Override
    public boolean removeComment(Long albumId, String commentId) {
        try {
            Long userId = AuthContextHolder.getUserId();
            if (userId == null) {
                return false;
            }

            String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.COMMENT, albumId);
            Query query = new Query(Criteria.where("id").is(commentId).and("userId").is(userId));

            Comment comment = mongoTemplate.findOne(query, Comment.class, collectionName);
            if (comment == null) {
                return false;
            }

            mongoTemplate.updateFirst(query, new Update().set("deleteMark", "1"), collectionName);
            return true;
        } catch (Exception e) {
            log.error("删除评论失败", e);
            return false;
        }
    }

    private boolean checkIsPraised(Long userId, String commentId) {
        String collectionName = MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.COMMENT_PRAISE, userId);
        Query query = new Query(Criteria.where("commentId").is(commentId));
        return mongoTemplate.exists(query, collectionName);
    }
}
