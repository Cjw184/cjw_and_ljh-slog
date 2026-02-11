package com.chenliao.chenliaoblog.config.mail;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
public class SendMailConfig {
    public static void sendMail(MailInfo mailInfo) {
        try {
            MailAccount account = new MailAccount();
            //邮件服务器的SMTP地址
            account.setHost("smtp.163.com");
            //邮件服务器的SMTP端口
            account.setPort(587);
            //发件人邮箱，改成你自己的
            account.setFrom("cjw18476714119@163.com");
            //密码，刚开通的授权码
            account.setPass("HYZFBWMvEQRbaq5D");
            //使用SSL安全连接
            account.setSslEnable(false);
            MailUtil.send(account, mailInfo.getReceiveMail(),
                    mailInfo.getTitle(), mailInfo.getContent(), false);
            log.info("邮件发送成功！");
        } catch (Exception e) {
            log.error("邮件发送失败" + JSONUtil.toJsonStr(mailInfo));
        }

    }


}
