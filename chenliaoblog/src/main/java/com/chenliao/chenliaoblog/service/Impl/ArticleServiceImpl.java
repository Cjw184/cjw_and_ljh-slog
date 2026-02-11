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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
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

    // 注入邮件发送专用线程池
    @Autowired
    @Qualifier("mailExecutor")
    private Executor mailExecutor;

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
        // ===== 新增：前置校验用户ID有效性，避免空指针 =====
        if (article.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空，无法发布文章");
        }
        User existUser = userMapper.getUserById(article.getUserId());
        if (existUser == null) {
            throw new IllegalArgumentException("用户ID=" + article.getUserId() + "不存在，请先创建用户");
        }

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

        // 5. 异步发送邮件提醒（替换原有同步逻辑）
        sendMailAsync(existUser, article, "文章发布");
    }

    @Override
    public void updateArticle(Article article) {
        // ===== 新增：前置校验用户ID有效性，避免空指针 =====
        if (article.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空，无法更新文章");
        }
        User existUser = userMapper.getUserById(article.getUserId());
        if (existUser == null) {
            throw new IllegalArgumentException("用户ID=" + article.getUserId() + "不存在");
        }

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

        // 5. 异步发送邮件提醒（替换原有同步逻辑）
        sendMailAsync(existUser, article, "文章更新");
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

    /**
     * 私有方法：异步发送邮件（核心新增逻辑）
     * @param user 收件用户（已校验非空）
     * @param article 文章信息
     * @param mailTitle 邮件标题（文章发布/更新）
     */
    private void sendMailAsync(User user, Article article, String mailTitle) {
        // 1. 校验用户邮箱是否有效
        boolean sendSuccess = false;
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.warn("用户{}（ID={}）未配置邮箱，跳过异步邮件发送", user.getUserName(), user.getId());
            return;
        }

        // 2. 提交异步任务到线程池
        mailExecutor.execute(() -> {
            try {
                String userName = user.getUserName() == null ? "用户" : user.getUserName();
                // 构建邮件内容
                String content = MessageFormat.format(
                        "【{0}】您好：\n您已成功{1}了标题为: {2} 的文章 \n请注意查收！\n",
                        userName,
                        "文章发布".equals(mailTitle) ? "发布" : "更新",
                        article.getTitle()
                );

                // 构建邮件信息
                MailInfo mailInfo = MailInfo.builder()
                        .receiveMail(user.getEmail().trim())
                        .content(content)
                        .title(mailTitle)
                        .build();

                // 调用邮件发送工具类（这里抛异常会被catch捕获）
                SendMailConfig.sendMail(mailInfo);

                // ===== 关键修复：仅在发送成功后打印成功日志 =====
                //Thread.sleep(1000);
                log.info("异步邮件发送成功！收件人：{}，标题：{}，文章ID：{}",
                        user.getEmail(), mailTitle, article.getId());
            } catch (Exception e) {
                // 区分认证失败、连接失败等不同异常，精准提示
                String errorType = e instanceof cn.hutool.extra.mail.MailException && e.getCause() instanceof javax.mail.AuthenticationFailedException
                        ? "SMTP认证失败（大概率是163授权码错误）" : "邮件发送异常";

                // 打印详细失败日志，包含异常类型
                log.error("异步邮件发送失败！{}，用户ID：{}，文章ID：{}，标题：{}，异常信息：{}",
                        errorType, user.getId(), article.getId(), mailTitle, e.getMessage(), e);
            }
        });
    }
}