package com.atfangyi.tingshu.album.api;

import com.atfangyi.tingshu.album.service.CommentService;
import com.atfangyi.tingshu.common.annotation.Login;
import com.atfangyi.tingshu.common.result.Result;
import com.atfangyi.tingshu.model.comment.Comment;
import com.atfangyi.tingshu.vo.comment.CommentVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评论管理")
@RestController
@RequestMapping("api/comment")
@SuppressWarnings({"all"})
public class CommentApiController {

    @Resource
    private CommentService commentService;

    @Operation(summary = "获取评论分页列表")
    @GetMapping("/findCommentPage/{albumId}/{trackId}/{page}/{limit}")
    public Result<IPage<CommentVo>> findCommentPage(
            @PathVariable Long albumId,
            @PathVariable Long trackId,
            @PathVariable Long page,
            @PathVariable Long limit) {
        return Result.ok(commentService.findCommentPage(albumId, trackId, page, limit));
    }

    @Operation(summary = "新增评论")
    @PostMapping("/save")
    @Login(required = true)
    public Result saveComment(@RequestBody Comment comment) {
        return commentService.saveComment(comment) ? Result.ok() : Result.fail();
    }

    @Operation(summary = "点赞/取消点赞评论")
    @GetMapping("/praise/{albumId}/{commentId}")
    @Login(required = true)
    public Result praiseComment(@PathVariable Long albumId, @PathVariable String commentId) {
        return commentService.praiseComment(albumId, commentId) ? Result.ok() : Result.fail();
    }

    @Operation(summary = "删除评论")
    @GetMapping("/remove/{albumId}/{commentId}")
    @Login(required = true)
    public Result removeComment(@PathVariable Long albumId, @PathVariable String commentId) {
        return commentService.removeComment(albumId, commentId) ? Result.ok() : Result.fail();
    }
}
