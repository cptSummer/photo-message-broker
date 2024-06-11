package org.profitsoft.photomessagebroker.database.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Getter
@Builder
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
public class EmailErrorDto {

    private ErrorMessageDto errorMessageDto;

    private Integer resentCount;

    private Instant lastResent;

}
