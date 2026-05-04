package com.atfangyi.tingshu.album.service;

import com.atfangyi.tingshu.model.comment.Comment;
import com.atfangyi.tingshu.vo.comment.CommentVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface CommentService {

    /**
     * 分页查询评论列表
     */
    IPage<CommentVo> findCommentPage(Long albumId, Long trackId, Long page, Long limit);

    /**
     * 保存评论
     */
    boolean saveComment(Comment comment);

    /**
     * 点赞/取消点赞评论
     */
    boolean praiseComment(Long albumId, String commentId);

    /**
     * 删除评论
     */
    boolean removeComment(Long albumId, String commentId);
}
