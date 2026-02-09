package com.chenliao.chenliaoblog.service.Impl;

import com.chenliao.chenliaoblog.config.page.PageRequest;
import com.chenliao.chenliaoblog.entity.Category;
import com.chenliao.chenliaoblog.service.CategoryService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.chenliao.chenliaoblog.mapper.CategoryMapper;


import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public List<Category> getCategoryPage(PageRequest pageRequest) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        PageHelper.startPage(pageNum,pageSize);
        List<Category> categoryList = categoryMapper.getCategoryPage();
        return categoryList;
    }

    @Override
    public int saveCategory(Category category) {
        return categoryMapper.create(category);
    }

    @Override
    public int updateCategory(Category category) {
        return categoryMapper.update(category);
    }

    @Override
    public void deleteCategory(Integer categoryId) {
        categoryMapper.delete(categoryId);
    }

    @Override
    public Category findById(Integer categoryId) {
        Category category = categoryMapper.getById(categoryId);
        if (category == null) {
            return null;
        }
        return category;
    }

}
