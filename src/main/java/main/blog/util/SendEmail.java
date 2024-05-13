package main.blog.util;

import com.google.gson.Gson;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class SendEmail {

    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private final SpringTemplateEngine templateEngine;

    public void sendEmailVerification(String emailName, String verificationLink) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("name", emailName);
        emailValues.put("verificationLink", verificationLink);

        Context context = new Context();
        emailValues.forEach((key, value)->{
            context.setVariable(key, value);
        });

        String html = templateEngine.process("mail/verification-email", context);
        helper.setText(html, true);
        helper.setTo(emailName);
        helper.setFrom("paosidu@naver.com");
        helper.setSubject("videomanager 회원인증");

        emailSender.send(message);
    }


    public void sendPasswordVerification(String emailName, String passwordResetLink) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        HashMap<String, String> emailValues = new HashMap<>();
        emailValues.put("name", emailName);
        emailValues.put("passwordResetLink", passwordResetLink);

        Context context = new Context();
        emailValues.forEach((key, value)->{
            context.setVariable(key, value);
        });

        String html = templateEngine.process("mail/verification-password", context);
        helper.setText(html, true);
        helper.setTo(emailName);
        helper.setFrom("paosidu@naver.com");
        helper.setSubject("videomanager 비밀번호 재설정");

        emailSender.send(message);
    }
}
