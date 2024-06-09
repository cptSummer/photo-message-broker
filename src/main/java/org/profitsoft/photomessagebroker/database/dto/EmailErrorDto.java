package org.profitsoft.photomessagebroker.database.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Getter
@Builder
@Jacksonized
public class EmailErrorDto {

    private ErrorMessageDto errorMessageDto;

    private Integer resentCount;

    private Instant lastResent;

}
