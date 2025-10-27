package com.limuzi.limuziaicodeuser.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class SendCode {

    private final JavaMailSender mailSender;

    // 从配置读取发件人邮箱（推荐与 spring.mail.username 保持一致）
    @Value("${spring.mail.username}")
    private String fromAddress;

    /**
     * 生成 6 位随机验证码
     */
    public String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000; // 100000-999999
        return String.valueOf(code);
    }

    /**
     * 发送邮箱验证码（保守推荐：From 只使用纯邮箱地址）
     * @param to 收件人邮箱
     * @param code 验证码
     */
    public void sendVerificationCode(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        // false 表示不是 multipart，"UTF-8" 保证编码
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        // 推荐：只用纯邮箱地址作为 From（与 SMTP 登录账号一致）
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject("【LimuziAI】邮箱验证码");

        String content = "<html><body>"
                + "<h3>您好！</h3>"
                + "<p>您的验证码为：<b style='color:#1E90FF;font-size:20px;'>" + code + "</b></p>"
                + "<p>有效期 5 分钟，请尽快使用。</p>"
                + "<p style='color:gray'>如果不是您本人操作，请忽略此邮件。</p>"
                + "</body></html>";

        helper.setText(content, true);
        mailSender.send(message);
    }

    /**
     * 可选：带显示名的发送（如果你确实想显示中文名），注意使用 InternetAddress 指定编码
     */
    public void sendWithPersonalName(String to, String code, String personalName) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();

        // 设置带编码的 From（注意：有些 SMTP 要求 From 必须等于登录邮箱）
        message.setFrom(new InternetAddress(fromAddress, personalName, "UTF-8"));

        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setTo(to);
        helper.setSubject("【LimuziAI】邮箱验证码");
        helper.setText("<p>验证码：" + code + "</p>", true);
        mailSender.send(message);
    }
}
