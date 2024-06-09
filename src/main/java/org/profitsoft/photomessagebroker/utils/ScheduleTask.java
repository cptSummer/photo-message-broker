package org.profitsoft.photomessagebroker.utils;

import lombok.RequiredArgsConstructor;
import org.profitsoft.photomessagebroker.database.service.EmailService;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
public class ScheduleTask {
    private final EmailService emailService;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void scheduleTaskWithFixedDelay() {
        messageChecker();
    }

    private void messageChecker() {
        emailService.checkErrorStatus();
    }
}
