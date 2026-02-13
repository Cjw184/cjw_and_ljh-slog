package com.chenliao.chenliaoblog.service.Impl;

import com.chenliao.chenliaoblog.config.page.PageRequest;
import com.chenliao.chenliaoblog.entity.OperationLog;
import com.chenliao.chenliaoblog.mapper.OperationLogMapper;
import com.chenliao.chenliaoblog.service.OperationLogService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationLogServiceImpl implements OperationLogService {
    @Autowired
    OperationLogMapper operationLogMapper;

    @Override
    public void saveOperationLog(OperationLog operationLog) {
        operationLogMapper.createOperationLog(operationLog);
    }

    @Override
    public List<OperationLog> getOperationLogPage(PageRequest pageRequest) {
        int pageNum = pageRequest.getPageNum();
        int pageSize = pageRequest.getPageSize();
        PageHelper.startPage(pageNum,pageSize);
        List<OperationLog> operationLogList = operationLogMapper.getOperationLogPage();
        return operationLogList;
    }

}
