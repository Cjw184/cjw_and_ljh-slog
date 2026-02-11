package com.chenliao.chenliaoblog.mapper;

import com.chenliao.chenliaoblog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagMapper {
    /**
     * 创建
     * @param tag
     * @return
     */
    int createTag(Tag tag);

    /**
     * 修改
     * @param tag
     * @return
     */
    int updateTag(Tag tag);

    /**
     * 分类列表（分页）
     * @return
     */
    List<Tag> getTagPage();

    /**
     * 删除
     * @param id
     */
    void deleteTag(Integer id);

    /**
     * 批量添加标签
     * @param strings
     * @return
     */
    boolean batchAddTag(List<Tag> strings);

    /**
     * 批量删除标签
     * @param ids
     */
    boolean deleteBatch(List<Tag> ids);

    /**
     * 根据标签查找该对象
     * @param tag
     * @return
     */
    Tag getByTagName(Tag tag);

    Tag getTagById(Integer id);

}
