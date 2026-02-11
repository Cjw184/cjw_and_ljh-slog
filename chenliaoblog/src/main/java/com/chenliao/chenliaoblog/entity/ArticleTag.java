package com.chenliao.chenliaoblog.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleTag {
    /**
     * id主键
     */
    private Integer id;

    /**
     * 文章id
     */
    private Integer articleId;

    /**
     * 标签id
     */
    private Integer tagId;

}
