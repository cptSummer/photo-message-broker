package org.profitsoft.photomessagebroker.database.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorMessageDto {

    private String message;
    private String errorClass;
}
