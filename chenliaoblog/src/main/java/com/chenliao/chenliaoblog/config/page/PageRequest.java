package com.chenliao.chenliaoblog.config.page;


import lombok.Data;

@Data
public class PageRequest {
    /**
     * 页码
     */
    private int pageNum;

    /**
     * 每页的数据条数
     */
    private int pageSize;
}
