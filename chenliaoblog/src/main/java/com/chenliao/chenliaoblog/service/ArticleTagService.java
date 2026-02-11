package com.chenliao.chenliaoblog.service;

import com.chenliao.chenliaoblog.entity.ArticleTag;

import java.util.List;

public interface ArticleTagService {
    /**
     * 批量插入文章标签数据
     *
     * @param articleTagList
     */
    void insertBatch(List<ArticleTag> articleTagList);

    void deleteTag(Integer articleId);

    List<ArticleTag> findArticleTagById(Integer articleId);
}
