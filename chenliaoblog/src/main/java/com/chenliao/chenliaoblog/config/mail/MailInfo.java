package com.chenliao.chenliaoblog.config.mail;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MailInfo {
    /**
     * 接收的邮箱
     */
    private String receiveMail;

    /**
     * 邮件标题
     */
    private String title;

    /**
     * 邮件内容
     */
    private String content;
}
