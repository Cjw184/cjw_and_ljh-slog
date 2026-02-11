package com.chenliao.chenliaoblog.config.mail;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SendMailConfig {
    public static void sendMail(MailInfo mailInfo) {
        try {
            MailAccount account = new MailAccount();
            // 核心修改：换465端口+开启SSL
            account.setHost("smtp.163.com");
            account.setPort(465); // 587→465
            account.setSslEnable(true); // false→true
            // 必须显式开启认证（默认true，但显式设置更稳妥）
            account.setAuth(true);

            account.setFrom("cjw18476714119@163.com"); // 你的163发件邮箱
            account.setPass("HYZFBWMvEQRbaq5D"); // 注意：是授权码，不是登录密码！

            MailUtil.send(account, mailInfo.getReceiveMail(),
                    mailInfo.getTitle(), mailInfo.getContent(), false);
            log.info("邮件发送成功！收件人：{}", mailInfo.getReceiveMail());
        } catch (Exception e) {
           // log.error("邮件发送失败，邮件信息：{}", JSONUtil.toJsonStr(mailInfo), e);
            throw e;
        }
    }
}