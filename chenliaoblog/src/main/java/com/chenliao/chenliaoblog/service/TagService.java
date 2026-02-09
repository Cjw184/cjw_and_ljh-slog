package com.chenliao.chenliaoblog.service;

import com.chenliao.chenliaoblog.config.page.PageRequest;
import com.chenliao.chenliaoblog.entity.Tag;

import java.util.List;

public interface TagService {
    /**
     * 获取所有的标签（分页）
     * @return
     */
    List<Tag> getTagPage(PageRequest pageRequest);

    /**
     * 新建标签
     * @param tag
     * @return
     */
    int saveTag(Tag tag);

    /**
     * 修改标签
     * @param tag
     * @return
     */
    int updateTag(Tag tag);

    /**
     * 删除标签
     * @param tagId
     */
    void deleteTag(Integer tagId);

    /**
     * 批量添加
     * @param tags
     * @return
     */
    boolean batchAddTag(String tags) throws Exception;

    /**
     * 批量删除标签
     * @param ids
     * @return
     */
    boolean batchDelTag(String ids);

    /**
     * 根据标签查找
     * @param tagName
     * @return
     */
    Tag findByTagName(String tagName);

}
