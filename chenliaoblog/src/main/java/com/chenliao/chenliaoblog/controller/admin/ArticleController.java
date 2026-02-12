package com.chenliao.chenliaoblog.controller.admin;

import com.chenliao.chenliaoblog.config.page.PageRequest;
import com.chenliao.chenliaoblog.config.page.PageResult;
import com.chenliao.chenliaoblog.entity.Article;
import com.chenliao.chenliaoblog.entity.User;
import com.chenliao.chenliaoblog.entity.dto.ArticleDTO;
import com.chenliao.chenliaoblog.service.ArticleService;
import com.chenliao.chenliaoblog.service.UserService;
import com.chenliao.chenliaoblog.utils.JsonResult;
import com.chenliao.chenliaoblog.utils.PageUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("adminArticleController")
@RequestMapping("/admin/article")
public class ArticleController {
    @Autowired
    ArticleService articleService;
    @Autowired
    UserService userService;

    /**
     * 文章列表
     * @param articleDTO
     * @return
     */
    @Operation(summary = "文章列表")
    @PostMapping("list")
    public JsonResult<Object> listPage(@RequestBody ArticleDTO articleDTO) {
        List<Article> articleList = articleService.getArticlePage(articleDTO);
        PageInfo pageInfo = new PageInfo(articleList);
        PageRequest pageRequest = new PageRequest();
        pageRequest.setPageNum(articleDTO.getPageNum());
        pageRequest.setPageSize(articleDTO.getPageSize());
        PageResult pageResult = PageUtil.getPageResult(pageRequest, pageInfo);
        return JsonResult.success(pageResult);
    }

    /**
     * 添加文章
     * @return
     */
    @Operation(summary = "添加文章")
    @PostMapping("/create")
    public JsonResult<Object> articleCreate(@RequestBody  Article article) {
        // 1. 校验用户ID是否为空
        if (article.getUserId() == null) {
            String errorMsg = "添加文章失败：用户ID不能为空，请先选择发布用户";
            log.warn(errorMsg);
            // 返回错误的JsonResult，符合你的统一响应格式
            return JsonResult.error(errorMsg);
        }

        // 2. 校验用户ID对应的用户是否存在
        User existUser = userService.findByUserId(article.getUserId());
        if (existUser == null) {
            String errorMsg = "添加文章失败：用户ID=" + article.getUserId() + "不存在，请先创建该用户";
            log.error(errorMsg);
            return JsonResult.error(errorMsg);
        }
        // 添加文章
        articleService.saveArticle(article);
        return JsonResult.success();
    }

    /**
     * 修改文章
     * @return
     */
    @Operation(summary = "修改文章")
    @PostMapping("/update")
    public JsonResult<Object> articleUpdate(@RequestBody  Article article) {
        articleService.updateArticle(article);
        return JsonResult.success();
    }

    /**
     * 删除文章
     * @return
     */
    @Operation(summary = "删除文章")
    @DeleteMapping("/delete/{id}")
    public JsonResult<Object> articleDelete(@PathVariable(value = "id") int id) {
        articleService.deleteArticle(id);
        return JsonResult.success();
    }

    /**
     * 根据文章id查找
     * @param id
     * @return
     */
    @Operation(summary = "根据文章id查找")
    @PostMapping("/getArticle/{id}")
    public JsonResult<Object> getArticleById(@PathVariable(value = "id") int id) {
        Article article = articleService.findById(id);
        return JsonResult.success(article);
    }



}
