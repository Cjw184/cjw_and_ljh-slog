package com.chenliao.chenliaoblog.mapper;

import com.chenliao.chenliaoblog.entity.Article;
import com.chenliao.chenliaoblog.entity.dto.ArticleDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper {
    /**
     * 查询所有的文章列表
     * @return
     */
    List<Article> findAll();

    /**
     * 创建文章
     * @param article
     * @return
     */
    int createArticle(Article article);

    /**
     * 修改文章
     * @param article
     * @return
     */
    int updateArticle(Article article);

    /**
     * 分类列表（分页）
     * @param articleDTO
     * @return
     */
    List<Article> getArticlePage(@Param("articleBO") ArticleDTO articleDTO);

    /**
     * 删除文章
     * @param id
     */
    void deleteArticle(Integer id);

    /**
     * 根据id查找分类
     * @param id
     * @return
     */
    Article getById(Integer id);

}
