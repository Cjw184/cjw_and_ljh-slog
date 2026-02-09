package com.chenliao.chenliaoblog.mapper;

import com.chenliao.chenliaoblog.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {
    /**
     * 创建
     * @param notice
     * @return
     */
    int createNotice(Notice notice);

    /**
     * 修改
     * @param notice
     * @return
     */
    int updateNotice(Notice notice);

    /**
     * 分类列表（分页）
     * @return
     */
    List<Notice> getNoticePage();

    /**
     * 删除
     * @param id
     */
    void deleteNotice(Integer id);


}
