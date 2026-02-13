package com.chenliao.chenliaoblog.mapper;

import com.chenliao.chenliaoblog.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OperationLogMapper {
    /**
     * 创建操作日志
     * @param operationLog
     * @return
     */
    int createOperationLog(OperationLog operationLog);

    /**
     * 分类列表（分页）
     * @return
     */
    List<OperationLog> getOperationLogPage();

}
