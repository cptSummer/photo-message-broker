package org.profitsoft.photomessagebroker.database.service;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.profitsoft.photomessagebroker.database.data.*;
import org.profitsoft.photomessagebroker.database.dto.*;
import org.profitsoft.photomessagebroker.database.repository.EmailRepository;

import org.profitsoft.photomessagebroker.utils.emailsentservice.EmailSentServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailRepository emailRepository;

    @Async
    @RabbitListener(queues = "${spring.rabbitmq.queue}")
    public void processMessage(EmailRecipientDto dto) {
        saveEmail(dto);
    }

    private void saveEmail(EmailRecipientDto dto) {
        try {
            EmailSaveDto emailSaveDto = getEmailSaveDtoByRecipient(dto);
            EmailData emailData = convertToNewEmailData(emailSaveDto);
            EmailData savedEmailData = emailRepository.save(emailData);
            sendEmail(savedEmailData);
        } catch (Exception e) {
            System.out.println("Error occurred while saving email\n with message: " + e.getMessage());
        }
    }

    private static EmailSaveDto getEmailSaveDtoByRecipient(EmailRecipientDto dto) {
        String senderName = "Entire Photos";
        String senderEmail = new JavaMailSenderImpl().getUsername();
        String subject = "Entire Photos: New User";
        String content = "Hello, " + dto.getName() + "!\n" + "Thank you for your registration.";
        EmailSenderDto emailSenderDto = new EmailSenderDto();
        emailSenderDto.setName(senderName);
        emailSenderDto.setEmail(senderEmail);
        return new EmailSaveDto(subject, content, emailSenderDto, dto, "SENT", null);
    }

    public void sendEmail(EmailData data) {
        try {
            EmailSentServiceImpl emailSentService = new EmailSentServiceImpl(new JavaMailSenderImpl());
            emailSentService.sendMessage(data.getFrom().getEmail(), data.getSubject(), data.getContent());

            if(data.getStatus().equals("ERROR")){
                data.setStatus("SENT");
                emailRepository.save(data);
            }
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
            ErrorMessageDto errorMessageDto = new ErrorMessageDto();
            errorMessageDto.setMessage(e.getMessage());
            errorMessageDto.setErrorClass(e.getClass().getName());

            Integer resentCount = data.getError().getResentCount();
            Instant lastResent = Instant.now();
            if (resentCount == null) {
                resentCount = 0;
            }else {
               resentCount++;
            }

            EmailErrorDto emailErrorDto = new EmailErrorDto(errorMessageDto, resentCount, lastResent);
            EmailSaveDto dto = getEmailSaveDtoByDataAndError(data, emailErrorDto);
            EmailData emailData = convertToNewEmailData(dto);
            emailRepository.save(emailData);
        }
    }

    private static EmailSaveDto getEmailSaveDtoByDataAndError(EmailData data, EmailErrorDto emailErrorDto) {
        EmailSenderDto emailSenderDto = new EmailSenderDto();
        emailSenderDto.setName(data.getFrom().getName());
        emailSenderDto.setEmail(data.getFrom().getEmail());
        EmailRecipientDto emailRecipientDto = new EmailRecipientDto();
        emailRecipientDto.setEmail(data.getRecipients().getEmail());
        emailRecipientDto.setName(data.getRecipients().getName());
        return new EmailSaveDto(
                data.getSubject(),
                data.getContent(),
                emailSenderDto,
                emailRecipientDto,
                "ERROR",
                emailErrorDto);
    }

    public void checkErrorStatus() {
        emailRepository.findAllByStatus("ERROR").forEach(this::sendEmail);
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
