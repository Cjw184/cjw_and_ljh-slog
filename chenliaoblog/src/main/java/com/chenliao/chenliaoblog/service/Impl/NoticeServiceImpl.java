package com.chenliao.chenliaoblog.service.Impl;

import com.chenliao.chenliaoblog.config.page.PageRequest;
import com.chenliao.chenliaoblog.entity.Notice;
import com.chenliao.chenliaoblog.mapper.NoticeMapper;
import com.chenliao.chenliaoblog.service.NoticeService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {
    @Autowired
    NoticeMapper noticeMapper;

    @Override
    public List<Notice> getNoticePage(PageRequest pageRequest) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        PageHelper.startPage(pageNum,pageSize);
        List<Notice> noticeList = noticeMapper.getNoticePage();
        return noticeList;
    }

    @Override
    public int saveNotice(Notice notice) {
        return noticeMapper.createNotice(notice);
    }

    @Override
    public int updateNotice(Notice notice) {
        return noticeMapper.updateNotice(notice);
    }

    @Override
    public void deleteNotice(Integer noticeId) {
        noticeMapper.deleteNotice(noticeId);
    }

}
