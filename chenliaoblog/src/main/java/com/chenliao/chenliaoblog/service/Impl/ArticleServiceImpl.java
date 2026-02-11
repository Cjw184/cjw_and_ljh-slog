package com.chenliao.chenliaoblog.service.Impl;

import com.chenliao.chenliaoblog.config.mail.MailInfo;
import com.chenliao.chenliaoblog.config.mail.SendMailConfig;
import com.chenliao.chenliaoblog.entity.ArticleTag;
import com.chenliao.chenliaoblog.entity.Tag;
import com.chenliao.chenliaoblog.entity.User;
import com.chenliao.chenliaoblog.entity.dto.ArticleDTO;

import com.chenliao.chenliaoblog.entity.Article;
import com.chenliao.chenliaoblog.mapper.ArticleMapper;
import com.chenliao.chenliaoblog.mapper.ArticleTagMapper;
import com.chenliao.chenliaoblog.mapper.TagMapper;
import com.chenliao.chenliaoblog.mapper.UserMapper;
import com.chenliao.chenliaoblog.service.ArticleService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Autowired
    private UserMapper userMapper;

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
    @Autowired
    private TagMapper tagMapper;

    @Override
    public List<Article> getArticlePage(ArticleDTO articleDTO) {
        int pageNum = articleDTO.getPageNum();
        int pageSize = articleDTO.getPageSize();
        PageHelper.startPage(pageNum,pageSize);
        List<Article> articleList = articleMapper.getArticlePage(articleDTO);
        List<Tag> tagList = new ArrayList<>();
        if (articleList != null) {
            for (Article article : articleList) {
                List<ArticleTag> articleTags = articleTagMapper.getArticleTagById(article.getId());
                if (articleTags != null) {
                    for (ArticleTag articleTag : articleTags) {
                        Tag tag = tagMapper.getTagById(articleTag.getTagId());
                        tagList.add(tag);
                    }
                }
                article.setTagList(tagList);
            }
        }
        return articleList;
    }

    @Override
    public void saveArticle(Article article) {
        // 1. 保存到数据库
        articleMapper.createArticle(article);
        //添加文章标签
        if (article.getTagIdList() != null) {
            List<ArticleTag> articleTagList = article.getTagIdList().stream().map(tagId -> ArticleTag.builder()
                    .tagId(tagId)
                    .articleId(article.getId())
                    .build()).collect(Collectors.toList());
            articleTagMapper.insertBatch(articleTagList);
        }
        // 2. 转换为JSON字符串，存入Redis
        String key = getArticleCacheKey(article.getId());
        try {
            String articleJson = objectMapper.writeValueAsString(article);
            stringRedisTemplate.opsForValue().set(key, articleJson, CACHE_EXPIRE_TIME, CACHE_TIME_UNIT);
            log.info("文章{}缓存新增成功（StringRedisTemplate）", article.getId());
        } catch (Exception e) {
            log.error("文章{}缓存新增失败", article.getId(), e);
        }
        User user = userMapper.getUserById(article.getUserId());
        //添加文章发送邮箱提醒
        String content = "【{0}】您好：\n" +
                "您已成功发布了标题为: {1} 的文章 \n" +
                "请注意查收！\n";
        MailInfo build = MailInfo.builder()
                .receiveMail(user.getEmail())
                .content(MessageFormat.format(content, user.getUserName(), article.getTitle()))
                .title("文章发布")
                .build();
        SendMailConfig.sendMail(build);
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
        //更新文章先把原来的标签删除掉
        articleTagMapper.deleteByArticleId(article.getId());
        //添加文章标签
        if (article.getTagIdList() != null) {
            List<ArticleTag> articleTagList = article.getTagIdList().stream().map(tagId -> ArticleTag.builder()
                    .tagId(tagId)
                    .articleId(article.getId())
                    .build()).collect(Collectors.toList());
            articleTagMapper.insertBatch(articleTagList);
        }
        User user = userMapper.getUserById(article.getUserId());
        //添加文章发送邮箱提醒
        String content = "【{0}】您好：\n" +
                "您已成功更新了标题为: {1} 的文章 \n" +
                "请注意查收！\n";
        MailInfo build = MailInfo.builder()
                .receiveMail(user.getEmail())
                .content(MessageFormat.format(content, user.getUserName(), article.getTitle()))
                .title("文章更新")
                .build();
        SendMailConfig.sendMail(build);


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
        //关联标签删除掉
        articleTagMapper.deleteByArticleId(articleId);
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
