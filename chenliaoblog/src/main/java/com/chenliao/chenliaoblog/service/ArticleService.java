package com.chenliao.chenliaoblog.service;

import com.chenliao.chenliaoblog.entity.Article;
import com.chenliao.chenliaoblog.entity.dto.ArticleDTO;

import java.util.List;

public interface ArticleService {
//    /**
//     * 初始化数据
//     */
//    void init();

    /**
     * 获取所有的文章（分页）
     * @return
     */
    List<Article> getArticlePage(ArticleDTO articleDTO);

    /**
     * 新建文章
     * @param article
     * @return
     */
    void saveArticle(Article article);

    /**
     * 修改文章
     * @param article
     * @return
     */
    void updateArticle(Article article);

    /**
     * 删除文章
     * @param articleId
     */
    void deleteArticle(Integer articleId);

    /**
     * 根据文章id查找文章
     * @param articleId
     * @return
     */
    Article findById(Integer articleId);

}
