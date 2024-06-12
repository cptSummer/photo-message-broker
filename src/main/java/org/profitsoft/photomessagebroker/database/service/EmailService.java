package org.profitsoft.photomessagebroker.database.service;

import lombok.RequiredArgsConstructor;

import org.profitsoft.photomessagebroker.database.data.*;
import org.profitsoft.photomessagebroker.database.dto.*;
import org.profitsoft.photomessagebroker.database.repository.EmailRepository;

import org.profitsoft.photomessagebroker.utils.emailsentservice.EmailSentServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailRepository emailRepository;

    @Autowired
    private EmailSentServiceImpl emailSentService;

    @Value("${spring.mail.username}")
    private String senderEmailRaw;

    /**
     * This method is responsible for processing incoming email recipient details from a RabbitMQ queue.
     * It saves the email and sends it asynchronously.
     *
     * @param dto The email recipient details.
     */
    @Async
    @RabbitListener(queues = "${spring.rabbitmq.queue}")
    public void processMessage(EmailRecipientDto dto) {
        System.out.println(dto.getEmail() + " " + dto.getName());
        saveEmail(dto);
    }

    /**
     * This method is responsible for saving an email and sending it.
     *
     * @param dto The email recipient details.
     */
    private void saveEmail(EmailRecipientDto dto) {
        EmailSaveDto emailSaveDto = getEmailSaveDtoByRecipient(dto);
        EmailData emailData = convertToNewEmailData(emailSaveDto);
        EmailData savedEmailData = emailRepository.save(emailData);
        sendEmail(savedEmailData);
    }
    /**
     * This method creates an EmailSaveDto object based on the provided EmailRecipientDto.
     * The EmailSaveDto object contains information about the email to be sent, including the sender, recipient, subject, content, status, and error.
     *
     * @param dto The EmailRecipientDto object containing the recipient information.
     * @return The EmailSaveDto object representing the email to be sent.
     */
    private EmailSaveDto getEmailSaveDtoByRecipient(EmailRecipientDto dto) {
        String senderName = "Entire Photos";
        String senderEmail = senderEmailRaw;

        String subject = "Entire Photos: New User";
        String content = "Hello, " + dto.getName() + "!\n" + "Thank you for your registration.";
        EmailSenderDto emailSenderDto = new EmailSenderDto();
        emailSenderDto.setName(senderName);
        emailSenderDto.setEmail(senderEmail);
        return new EmailSaveDto(subject, content, emailSenderDto, dto, "WAIT", null);
    }
    /**
     * Sends an email using the provided EmailData object.
     *
     * @param data The EmailData object containing the email details.
     */
    public void sendEmail(EmailData data) {
        try {
            emailSentService.sendMessage(data.getRecipients().getEmail(), data.getSubject(), data.getContent());
            updateEmailStatus(data.getId(), "SENT");
        } catch (Exception e) {
            handleEmailError(data, e);
        }
    }
    /**
     * Updates the status of an email based on the email ID.
     *
     * @param emailId The ID of the email to update.
     * @param status The new status to set for the email.
     */
    private void updateEmailStatus(String emailId, String status) {
        emailRepository.findById(emailId).ifPresent(
                emailData -> {
                    emailData.setStatus(status);
                    emailRepository.save(emailData);
                }
        );
    }

    /**
     * Handles the error that occurred during email processing.
     *
     * @param data The EmailData object containing email details.
     * @param e The Exception that occurred.
     */
    private void handleEmailError(EmailData data, Exception e) {
        ErrorMessageDto errorMessageDto = new ErrorMessageDto();
        errorMessageDto.setMessage(e.getMessage());
        errorMessageDto.setErrorClass(e.getClass().getName());

        Integer resentCount = data.getError().getResentCount() == null ? 0 : data.getError().getResentCount() + 1;
        Instant lastResent = Instant.now();

        EmailErrorDto emailErrorDto = new EmailErrorDto(errorMessageDto, resentCount, lastResent);
        EmailSaveDto dto = getEmailSaveDtoByDataAndError(data, emailErrorDto);
        EmailData emailData = convertToNewEmailData(dto);
        emailData.setId(data.getId());

        emailRepository.findById(emailData.getId()).ifPresent(
                existingEmailData -> {
                    existingEmailData.setError(emailData.getError());
                    emailRepository.save(existingEmailData);
                }
        );
    }
    /**
     * This method creates an EmailSaveDto object based on the provided EmailData and EmailErrorDto.
     * It constructs the dto with the necessary sender, recipient, subject, content, status, and error details.
     *
     * @param data         The EmailData object containing email data.
     * @param emailErrorDto The EmailErrorDto object containing error information.
     * @return The EmailSaveDto object representing the email to be saved.
     */
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
