package org.profitsoft.photomessagebroker;


import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.profitsoft.photomessagebroker.database.data.EmailData;
import org.profitsoft.photomessagebroker.database.data.EmailRecipient;
import org.profitsoft.photomessagebroker.database.data.EmailSender;
import org.profitsoft.photomessagebroker.database.repository.EmailRepository;
import org.profitsoft.photomessagebroker.database.service.EmailService;
import org.profitsoft.photomessagebroker.utils.emailsentservice.EmailSentServiceImpl;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.MessagingException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class EmailServiceTest {

    @MockBean
    private EmailService emailService = Mockito.mock(EmailService.class);

    @MockBean
    private EmailSentServiceImpl emailSentServiceMock = Mockito.mock(EmailSentServiceImpl.class);

    @Test
    public void testSuccessSend() {
        EmailData emailData = getEmailData();
        emailData.setStatus("SENT");
        emailService.sendEmail(emailData);

        verify(emailService, times(1)).sendEmail(emailData);
        assertNotEquals("ERROR", emailData.getStatus());
    }


    @Test
    public void testSendEmailFailure() throws MessagingException {
        EmailData emailData = getEmailData();
        MessagingException messagingException = new MessagingException("Failed to send email");
        doThrow(messagingException).when(emailSentServiceMock).sendMessage(anyString(), anyString(), anyString());

        try {
            emailService.sendEmail(emailData);
        } catch (Exception e) {
            assertTrue(e instanceof MessagingException);
            assertEquals(messagingException, e);
            assertEquals("ERROR", emailData.getStatus());
        }

        verify(emailSentServiceMock, times(0)).sendMessage(emailData.getFrom().getEmail(),
                emailData.getSubject(),
                emailData.getContent());
    }

    private static EmailData getEmailData() {
        EmailSender sender = new EmailSender();
        sender.setEmail("johndoe@example.com");
        sender.setName("John Doe");

        EmailRecipient recipient = new EmailRecipient();
        recipient.setEmail("janedoe@example.com");
        recipient.setName("Jane Doe");

        EmailData emailData = new EmailData();
        emailData.setFrom(sender);
        emailData.setRecipients(recipient);
        emailData.setSubject("Test email");
        emailData.setContent("This is a test email.");

        return emailData;
    }
}
