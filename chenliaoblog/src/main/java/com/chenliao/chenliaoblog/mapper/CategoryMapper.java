package com.chenliao.chenliaoblog.mapper;

import com.chenliao.chenliaoblog.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 创建
     * @param category
     * @return
     */
    int create(Category category);

    /**
     * 修改
     * @param category
     * @return
     */
    int update(Category category);

    /**
     * 分类列表（分页）
     * @return
     */
    List<Category> getCategoryPage();

    /**
     * 删除
     * @param id
     */
    void delete(Integer id);

    /**
     * 根据id查找分类
     * @param id
     * @return
     */
    Category getById(Integer id);

}
