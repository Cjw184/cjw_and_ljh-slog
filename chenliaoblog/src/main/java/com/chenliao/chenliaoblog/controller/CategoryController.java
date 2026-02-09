package com.chenliao.chenliaoblog.controller;


import com.chenliao.chenliaoblog.config.page.PageRequest;
import com.chenliao.chenliaoblog.config.page.PageResult;
import com.chenliao.chenliaoblog.entity.Category;
import com.chenliao.chenliaoblog.service.CategoryService;
import com.chenliao.chenliaoblog.utils.JsonResult;
import com.chenliao.chenliaoblog.utils.PageUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Api(tags = "分类管理")
@Controller
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    /**
     * 分页查询列表
     * @param pageRequest
     * @return
     */
    @PostMapping("list")
    public JsonResult<Object> listPage(@RequestBody  PageRequest pageRequest) {
        List<Category> categoryList = categoryService.getCategoryPage(pageRequest);
        PageInfo pageInfo = new PageInfo(categoryList);
        PageResult pageResult = PageUtil.getPageResult(pageRequest, pageInfo);
        return JsonResult.success(pageResult);
    }

    /**
     * 添加分类
     * @return
     */
    @ApiOperation(value = "添加分类")
    @PostMapping("/create")
    public JsonResult<Object> categoryCreate(@RequestBody  Category category) {
        int isStatus = categoryService.saveCategory(category);
        if (isStatus == 0) {
            return JsonResult.error("添加分类失败");
        }
        return JsonResult.success();
    }

    /**
     * 修改分类
     * @return
     */
    @ApiOperation(value = "修改分类")
    @PostMapping("/update")
    public JsonResult<Object> categoryUpdate(@RequestBody  Category category) {
        int isStatus = categoryService.updateCategory(category);
        if (isStatus == 0) {
            return JsonResult.error("修改分类失败");
        }
        return JsonResult.success();
    }

    /**
     * 删除
     * @return
     */
    @ApiOperation(value = "删除分类")
    @PostMapping("/delete/{id}")
    public JsonResult<Object> categoryDelete(@PathVariable(value = "id") int id) {
        categoryService.deleteCategory(id);
        return JsonResult.success();
    }


}
