package com.atfangyi.tingshu.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "专辑评论")
public class CommentVo {

    @Schema(description = "评论id")
    private String id;

    @Positive(message = "专辑id不能为空")
    @Schema(description = "专辑id")
    private Long albumId;

    @Schema(description = "声音id")
    private Long trackId;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatarUrl;

    @NotEmpty(message = "评论内容不能为空")
    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "被回复的评论id，一级评论默认为null")
    private String replyCommentId;

    @Schema(description = "点赞数量")
    private Integer praiseCount = 0;

    @Positive(message = "评分不能为空")
    @Schema(description = "评论中对专辑的评分 （十分制，建议采用五星制，如10分显示五颗星，7分显示三颗半星）")
    private Integer albumCommentScore;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "当前用户是否点赞")
    private Boolean isPraise;

    @Schema(description = "删除标记，1表示已删除")
    private String deleteMark;

    @Schema(description = "回复评论列表")
    private List<CommentVo> replyCommentList;

}
