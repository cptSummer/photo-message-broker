package org.profitsoft.photomessagebroker.database.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.extern.jackson.Jacksonized;


@Getter
@Builder
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
public class EmailSaveDto {

    @NotBlank(message = "Subject cannot be empty")
    private String subject;
    @NotBlank(message = "Content cannot be empty")
    private String content;

    private EmailSenderDto emailSenderDto;

    @NotBlank(message = "Recipients cannot be empty")
    private EmailRecipientDto emailRecipientDto;
    @NotBlank(message = "Status cannot be empty")
    private String status;

    private EmailErrorDto emailErrorDto;
}
