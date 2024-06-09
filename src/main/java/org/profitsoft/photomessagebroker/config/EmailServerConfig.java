package org.profitsoft.photomessagebroker.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;


@Configuration
@Data
public class EmailServerConfig {

    @Value("${SPRING_MAIL_HOST}")
    private String host;

    @Value("${SPRING_MAIL_PORT}")
    private int port;

    @Value("${SPRING_MAIL_USERNAME}")
    private String username;

    @Value("${SPRING_MAIL_PASSWORD}")
    private String password;

    @Value("${SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH}")
    private boolean smtpAuth;

    @Value("${SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE}")
    private boolean startTlsEnable;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.starttls.enable", startTlsEnable);
        props.put("mail.debug", "true");

        return mailSender;
    }


}
