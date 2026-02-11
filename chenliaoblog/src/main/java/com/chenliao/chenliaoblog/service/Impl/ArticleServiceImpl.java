package com.chenliao.chenliaoblog.service.Impl;

import com.chenliao.chenliaoblog.config.mail.MailInfo;
import com.chenliao.chenliaoblog.config.mail.SendMailConfig;
import com.chenliao.chenliaoblog.entity.Article;
import com.chenliao.chenliaoblog.entity.ArticleTag;
import com.chenliao.chenliaoblog.entity.Tag;
import com.chenliao.chenliaoblog.entity.User;
import com.chenliao.chenliaoblog.entity.dto.ArticleDTO;
import com.chenliao.chenliaoblog.mapper.ArticleMapper;
import com.chenliao.chenliaoblog.mapper.ArticleTagMapper;
import com.chenliao.chenliaoblog.mapper.TagMapper;
import com.chenliao.chenliaoblog.mapper.UserMapper;
import com.chenliao.chenliaoblog.service.ArticleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TagMapper tagMapper;

    // Jackson对象转换器（确保List<Tag>能正常序列化/反序列化）
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Redis缓存配置
    private static final String ARTICLE_CACHE_KEY_PREFIX = "chenliaoblog:article:";
    private static final long CACHE_EXPIRE_TIME = 24L;
    private static final TimeUnit CACHE_TIME_UNIT = TimeUnit.HOURS;

    /**
     * 通用方法：根据文章ID填充标签列表（核心修改：提取复用逻辑）
     */
    private void fillArticleTagList(Article article) {
        if (article == null || article.getId() == null) {
            return;
        }
        // 1. 查询文章关联的标签ID
        List<ArticleTag> articleTags = articleTagMapper.getArticleTagById(article.getId());
        List<Tag> tagList = new ArrayList<>();
        if (articleTags != null && !articleTags.isEmpty()) {
            // 2. 遍历标签ID，查询标签详情
            for (ArticleTag articleTag : articleTags) {
                Tag tag = tagMapper.getTagById(articleTag.getTagId());
                if (tag != null) { // 避免空标签
                    tagList.add(tag);
                }
            }
        }
        // 3. 填充到文章的tagList字段
        article.setTagList(tagList);
    }

    /**
     * 构建缓存Key
     */
    private String getArticleCacheKey(Integer articleId) {
        return ARTICLE_CACHE_KEY_PREFIX + articleId;
    }

    @Override
    public List<Article> getArticlePage(ArticleDTO articleDTO) {
        int pageNum = articleDTO.getPageNum();
        int pageSize = articleDTO.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        List<Article> articleList = articleMapper.getArticlePage(articleDTO);

        if (articleList != null && !articleList.isEmpty()) {
            // 修复bug：每个文章单独创建标签列表，避免复用
            for (Article article : articleList) {
                fillArticleTagList(article); // 复用填充标签的逻辑
            }
        }
        return articleList;
    }

//    //先查缓存的分页查询，暂不开启
//    @Override
//    public List<Article> getArticlePage(ArticleDTO articleDTO) {
//        int pageNum = articleDTO.getPageNum();
//        int pageSize = articleDTO.getPageSize();
//        PageHelper.startPage(pageNum, pageSize);
//
//        // 步骤1：先查数据库获取分页的「文章ID列表」（只查ID，性能最优）
//        List<Integer> articleIdList = articleMapper.getArticleIdPage(articleDTO);
//        if (articleIdList == null || articleIdList.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        List<Article> articleList = new ArrayList<>();
//        // 步骤2：批量从Redis获取文章（复用单篇缓存，包含tagList）
//        for (Integer articleId : articleIdList) {
//            Article article = null;
//            String key = getArticleCacheKey(articleId);
//            try {
//                // 先查Redis
//                String articleJson = stringRedisTemplate.opsForValue().get(key);
//                if (articleJson != null && !articleJson.isEmpty()) {
//                    article = objectMapper.readValue(articleJson, Article.class);
//                    log.info("分页查询：从Redis获取文章{}成功", articleId);
//                    articleList.add(article);
//                    continue;
//                }
//            } catch (Exception e) {
//                log.error("分页查询：从Redis获取文章{}失败", articleId, e);
//            }
//
//            // 步骤3：Redis未命中，查数据库+填充标签+回填Redis
//            article = articleMapper.getById(articleId);
//            if (article != null) {
//                fillArticleTagList(article); // 填充标签
//                // 回填Redis
//                try {
//                    String articleJson = objectMapper.writeValueAsString(article);
//                    stringRedisTemplate.opsForValue().set(key, articleJson, CACHE_EXPIRE_TIME, CACHE_TIME_UNIT);
//                    log.info("分页查询：文章{}缓存回填成功", articleId);
//                } catch (Exception e) {
//                    log.error("分页查询：文章{}缓存回填失败", articleId, e);
//                }
//                articleList.add(article);
//            }
//        }
//
//        return articleList;
//    }

    @Override
    public void saveArticle(Article article) {
        // 1. 保存文章到数据库
        articleMapper.createArticle(article);

        // 2. 保存文章标签关联关系
        if (article.getTagIdList() != null && !article.getTagIdList().isEmpty()) {
            List<ArticleTag> articleTagList = article.getTagIdList().stream()
                    .map(tagId -> ArticleTag.builder()
                            .tagId(tagId)
                            .articleId(article.getId())
                            .build())
                    .collect(Collectors.toList());
            articleTagMapper.insertBatch(articleTagList);
        }

        // 3. 填充标签列表（关键：存入Redis前先填充tagList）
        fillArticleTagList(article);

        // 4. 存入Redis（此时article包含完整的tagList）
        String key = getArticleCacheKey(article.getId());
        try {
            String articleJson = objectMapper.writeValueAsString(article);
            stringRedisTemplate.opsForValue().set(key, articleJson, CACHE_EXPIRE_TIME, CACHE_TIME_UNIT);
            log.info("文章{}缓存新增成功，包含标签列表：{}", article.getId(), article.getTagList());
        } catch (Exception e) {
            log.error("文章{}缓存新增失败", article.getId(), e);
        }

        // 5. 发送邮件提醒
        User user = userMapper.getUserById(article.getUserId());
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
        // 1. 更新文章到数据库
        articleMapper.updateArticle(article);

        // 2. 更新标签关联关系（先删后加）
        articleTagMapper.deleteByArticleId(article.getId());
        if (article.getTagIdList() != null && !article.getTagIdList().isEmpty()) {
            List<ArticleTag> articleTagList = article.getTagIdList().stream()
                    .map(tagId -> ArticleTag.builder()
                            .tagId(tagId)
                            .articleId(article.getId())
                            .build())
                    .collect(Collectors.toList());
            articleTagMapper.insertBatch(articleTagList);
        }

        // 3. 重新填充标签列表（确保最新标签）
        fillArticleTagList(article);

        // 4. 更新Redis缓存（覆盖旧值，包含最新标签）
        String key = getArticleCacheKey(article.getId());
        try {
            String articleJson = objectMapper.writeValueAsString(article);
            stringRedisTemplate.opsForValue().set(key, articleJson, CACHE_EXPIRE_TIME, CACHE_TIME_UNIT);
            log.info("文章{}缓存更新成功，包含最新标签列表：{}", article.getId(), article.getTagList());
        } catch (Exception e) {
            log.error("文章{}缓存更新失败", article.getId(), e);
        }

        // 5. 发送邮件提醒
        User user = userMapper.getUserById(article.getUserId());
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
        articleTagMapper.deleteByArticleId(articleId);

        // 2. 删除Redis缓存
        String key = getArticleCacheKey(articleId);
        try {
            stringRedisTemplate.delete(key);
            log.info("文章{}缓存删除成功", articleId);
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

        // 1. 先查Redis缓存
        try {
            String articleJson = stringRedisTemplate.opsForValue().get(key);
            if (articleJson != null && !articleJson.isEmpty()) {
                // 反序列化：自动解析tagList（List<Tag>）
                article = objectMapper.readValue(articleJson, Article.class);
                log.info("从Redis缓存获取文章{}成功，包含标签列表：{}", articleId, article.getTagList());
                return article;
            }
        } catch (Exception e) {
            log.error("从Redis缓存获取文章{}失败，降级查询数据库", articleId, e);
        }

        // 2. 缓存未命中，查数据库
        article = articleMapper.getById(articleId);
        if (article != null) {
            // 3. 填充标签列表（关键：数据库查询后先补全标签）
            fillArticleTagList(article);
            // 4. 缓存回填（存入包含标签的完整文章）
            try {
                String articleJson = objectMapper.writeValueAsString(article);
                stringRedisTemplate.opsForValue().set(key, articleJson, CACHE_EXPIRE_TIME, CACHE_TIME_UNIT);
                log.info("文章{}缓存回填成功，包含标签列表：{}", articleId, article.getTagList());
            } catch (Exception e) {
                log.error("文章{}缓存回填失败", articleId, e);
            }
        }
        return article;
    }
}