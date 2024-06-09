package org.profitsoft.photomessagebroker.database.service;

import lombok.RequiredArgsConstructor;

import org.profitsoft.photomessagebroker.database.data.*;
import org.profitsoft.photomessagebroker.database.dto.*;
import org.profitsoft.photomessagebroker.database.repository.EmailRepository;

import org.profitsoft.photomessagebroker.utils.emailsentservice.EmailSentServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailRepository emailRepository;

    @RabbitListener(queues = "${spring.rabbitmq.queue}")
    public void processMessage(EmailRecipientDto dto) {
        saveEmail(dto);
    }

    public void saveEmail(EmailRecipientDto dto) {
        try {
            String senderName = "Entire Photos";
            String senderEmail = new JavaMailSenderImpl().getUsername();
            String subject = "Entire Photos: New User";
            String content = "Hello, " + dto.getName() + "!\n" + "Thank you for your registration.";
            EmailSenderDto emailSenderDto = new EmailSenderDto();
            emailSenderDto.setName(senderName);
            emailSenderDto.setEmail(senderEmail);
            EmailSaveDto emailSaveDto = new EmailSaveDto(subject, content, emailSenderDto, dto, "SENT", null);
            EmailData emailData = convertToNewEmailData(emailSaveDto);
            EmailData savedEmailData = emailRepository.save(emailData);
            sendEmail(dto.getEmail(), subject, content);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void sendEmail(String email, String subject, String content) {
        EmailSentServiceImpl emailSentService = new EmailSentServiceImpl();
        emailSentService.sendMessage(email, subject, content);
    }

    public void checkErrorStatus() {
        emailRepository.findAllByStatus("ERROR").forEach(emailData -> {
            sendEmail(emailData.getFrom().getEmail(), emailData.getSubject(), emailData.getContent());
        });

    }


    private EmailData convertToNewEmailData(EmailSaveDto emailSaveDto) {
        EmailData emailData = new EmailData();
        updateEmailDataFromDto(emailData, emailSaveDto);
        return emailData;
    }

    private void updateEmailDataFromDto(EmailData data, EmailSaveDto dto) {
        data.setContent(dto.getContent());
        data.setSubject(dto.getSubject());
        data.setStatus(dto.getStatus());

        data.setRecipients(convertToEmailRecipient(dto.getEmailRecipientDto()));
        data.setFrom(convertToEmailSender(dto.getEmailSenderDto()));

        if (dto.getEmailErrorDto() != null) {
            data.setError(convertToEmailError(dto.getEmailErrorDto()));
        }

        data.setTimeSent(Instant.now());
    }


    private static EmailError convertToEmailError(EmailErrorDto dto) {
        EmailError emailError = new EmailError();
        emailError.setErrorMessage(convertToErrorMessage(dto.getErrorMessageDto()));
        emailError.setResentCount(dto.getResentCount());
        emailError.setLastResent(Instant.now());
        return emailError;
    }

    private static ErrorMessage convertToErrorMessage(ErrorMessageDto dto) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(dto.getMessage());
        errorMessage.setErrorClass(dto.getErrorClass());
        return errorMessage;
    }

    private static EmailSender convertToEmailSender(EmailSenderDto dto) {
        EmailSender emailSender = new EmailSender();
        emailSender.setEmail(dto.getEmail());
        emailSender.setName(dto.getName());
        return emailSender;
    }

    private static EmailRecipient convertToEmailRecipient(EmailRecipientDto dto) {
        EmailRecipient emailRecipient = new EmailRecipient();
        emailRecipient.setEmail(dto.getEmail());
        emailRecipient.setName(dto.getName());
        return emailRecipient;
    }

}
