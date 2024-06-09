package org.profitsoft.photomessagebroker.utils.emailsentservice;

public interface EmailSentService {

    void sendMessage(String to, String subject, String text);
}
