package com.chenliao.chenliaoblog.service.Impl;

import com.chenliao.chenliaoblog.entity.dto.ArticleDTO;

import com.chenliao.chenliaoblog.entity.Article;
import com.chenliao.chenliaoblog.mapper.ArticleMapper;
import com.chenliao.chenliaoblog.service.ArticleService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;

    // 改用StringRedisTemplate（默认String序列化，无需自定义序列化器）
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // Jackson对象转换器（用于对象和JSON字符串互转）
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Redis缓存Key前缀
    private static final String ARTICLE_CACHE_KEY_PREFIX = "chenliaoblog:article:";
    // 缓存过期时间（24小时）
    private static final long CACHE_EXPIRE_TIME = 24L;
    private static final TimeUnit CACHE_TIME_UNIT = TimeUnit.HOURS;

    @Override
    public List<Article> getArticlePage(ArticleDTO articleDTO) {
        int pageNum = articleDTO.getPageNum();
        int pageSize = articleDTO.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        List<Article> articleList = articleMapper.getArticlePage(articleDTO);
        return articleList;
    }

    @Override
    public void saveArticle(Article article) {
        // 1. 保存到数据库
        articleMapper.createArticle(article);
        // 2. 转换为JSON字符串，存入Redis
        String key = getArticleCacheKey(article.getId());
        try {
            String articleJson = objectMapper.writeValueAsString(article);
            stringRedisTemplate.opsForValue().set(key, articleJson, CACHE_EXPIRE_TIME, CACHE_TIME_UNIT);
            log.info("文章{}缓存新增成功（StringRedisTemplate）", article.getId());
        } catch (Exception e) {
            log.error("文章{}缓存新增失败", article.getId(), e);
        }
    }

    @Override
    public void updateArticle(Article article) {
        // 1. 更新数据库
        articleMapper.updateArticle(article);
        // 2. 同步更新Redis缓存（覆盖旧值）
        String key = getArticleCacheKey(article.getId());
        try {
            String articleJson = objectMapper.writeValueAsString(article);
            stringRedisTemplate.opsForValue().set(key, articleJson, CACHE_EXPIRE_TIME, CACHE_TIME_UNIT);
            log.info("文章{}缓存更新成功（StringRedisTemplate）", article.getId());
        } catch (Exception e) {
            log.error("文章{}缓存更新失败", article.getId(), e);
        }
    }

    @Override
    public void deleteArticle(Integer articleId) {
        // 1. 删除数据库数据
        articleMapper.deleteArticle(articleId);
        // 2. 删除Redis缓存
        String key = getArticleCacheKey(articleId);
        try {
            stringRedisTemplate.delete(key);
            log.info("文章{}缓存删除成功（StringRedisTemplate）", articleId);
        } catch (Exception e) {
            log.error("文章{}缓存删除失败", articleId, e);
        }
    }

    @Override
    public Article findById(Integer articleId) {
        if (articleId == null) {
            return null;
        }
        String key = getArticleCacheKey(articleId);
        Article article = null;

        // 1. 先查Redis缓存（获取JSON字符串）
        try {
            String articleJson = stringRedisTemplate.opsForValue().get(key);
            if (articleJson != null && !articleJson.isEmpty()) {
                // JSON字符串反序列化为Article对象
                article = objectMapper.readValue(articleJson, Article.class);
                log.info("从Redis缓存获取文章{}成功（StringRedisTemplate）", articleId);
                return article;
            }
        } catch (Exception e) {
            log.error("从Redis缓存获取文章{}失败，降级查询数据库", articleId, e);
        }

        // 2. 缓存未命中/失败，查数据库
        article = articleMapper.getById(articleId);
        if (article != null) {
            // 3. 数据库结果转JSON，写入Redis（缓存回填）
            try {
                String articleJson = objectMapper.writeValueAsString(article);
                stringRedisTemplate.opsForValue().set(key, articleJson, CACHE_EXPIRE_TIME, CACHE_TIME_UNIT);
                log.info("文章{}缓存回填成功（StringRedisTemplate）", articleId);
            } catch (Exception e) {
                log.error("文章{}缓存回填失败", articleId, e);
            }
        }
        return article;
    }

    /**
     * 构建缓存Key
     */
    private String getArticleCacheKey(Integer articleId) {
        return ARTICLE_CACHE_KEY_PREFIX + articleId;
    }
}
