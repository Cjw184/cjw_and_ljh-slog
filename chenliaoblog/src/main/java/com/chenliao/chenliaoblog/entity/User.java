package com.chenliao.chenliaoblog.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class User {

    /**
     * 主键id
     */
    //private Integer id;
    private Integer id;
    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private  String passWord;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 上次登录时间
     */
    @JsonFormat(pattern = "yyyyMMdd", timezone = "GMT+8")
    private Date lastLoginTime;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    //jwt令牌
    private String token;


}
