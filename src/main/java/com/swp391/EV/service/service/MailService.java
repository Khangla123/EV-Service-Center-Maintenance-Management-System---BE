package com.swp391.EV.service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailService {
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String from;

    public void sendNewPassword(String receiverEmail, String fullName, int age, String tempPassword) {
        try {
            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("age", age);
            context.setVariable("password", tempPassword);

            String text = templateEngine.process("greeting.html", context);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setPriority(1);
            helper.setSubject("Tài khoản EVService của bạn đã sẵn sàng");
            helper.setFrom(from);
            helper.setTo(receiverEmail);
            helper.setText(text, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendOtpEmail(String email, String otp) {
        try {
            Context context = new Context();
            context.setVariable("otpCode", otp);
            String text = templateEngine.process("otp", context);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setPriority(1);
            helper.setSubject("Mã otp để reset password");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setText(text, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
