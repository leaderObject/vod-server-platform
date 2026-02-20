package com.atfangyi.tingshu.dto;

import lombok.Data;

@Data
public class SearchDto {

    /**
     * 标题
     */
    private String albumTitle;

    /**
     * 一级标题
     */
    private Integer category1Id;

    /**
     * 二级标题
     */
    private Integer category2Id;

    /**
     * 三级标题
     */
    private Integer category3Id;

    /**
     * 作者
     */
    private String announcerName;


}
