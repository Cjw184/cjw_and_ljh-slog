package com.chenliao.chenliaoblog.service;

import com.chenliao.chenliaoblog.config.page.PageRequest;
import com.chenliao.chenliaoblog.entity.Notice;

import java.util.List;

public interface NoticeService {
    /**
     * 获取所有的分类（分页）
     * @return
     */
    List<Notice> getNoticePage(PageRequest pageRequest);

    /**
     * 新建分类
     * @param notice
     * @return
     */
    int saveNotice(Notice notice);

    /**
     * 修改分类
     * @param notice
     * @return
     */
    int updateNotice(Notice notice);

    /**
     * 删除分类
     * @param noticeId
     */
    void deleteNotice(Integer noticeId);

}
