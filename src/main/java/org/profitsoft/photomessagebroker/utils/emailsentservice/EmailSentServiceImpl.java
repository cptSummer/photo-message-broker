package org.profitsoft.photomessagebroker.utils.emailsentservice;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailSentServiceImpl implements EmailSentService {
    private final JavaMailSender emailSender;

    public EmailSentServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }
    /**
     * This method sends an email to the specified recipient with the given subject and text.
     *
     * @param to      The email address of the recipient.
     * @param subject The subject of the email.
     * @param text    The text of the email.
     */
    @Override
    public void sendMessage(String to, String subject, String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
