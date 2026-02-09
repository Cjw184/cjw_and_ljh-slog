package com.chenliao.chenliaoblog.config.page;

import lombok.Data;

import java.util.List;

@Data
public class PageResult {
    /**
     * 当前页码
     */
    private int pageNum;
    /**
     * 每页数量
     */
    private int pageSize;
    /**
     * 记录总数
     */
    private long totalSize;
    /**
     * 页码总数
     */
    private int totalPages;

    /**
     * 存放返回数据
     */
    private List<?> result;
}
