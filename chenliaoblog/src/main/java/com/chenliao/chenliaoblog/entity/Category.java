package com.chenliao.chenliaoblog.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Category {
    /**
     * 主键id
     */
    private Integer categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
