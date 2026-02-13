package com.chenliao.chenliaoblog.service;

import com.chenliao.chenliaoblog.config.page.PageRequest;
import com.chenliao.chenliaoblog.entity.OperationLog;

import java.util.List;

public interface OperationLogService {
    /**
     * 保存操作日志
     *
     * @param operationLog
     * @return
     */
    void saveOperationLog(OperationLog operationLog);

    /**
     * 操作日志列表（分页）
     *
     * @param pageRequest
     * @return
     */
    List<OperationLog> getOperationLogPage(PageRequest pageRequest);

}
