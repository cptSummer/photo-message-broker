package org.profitsoft.photomessagebroker.database.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class EmailError {
    @Field(type = FieldType.Object)
    private ErrorMessage errorMessage;

    @Field(type = FieldType.Integer)
    private Integer resentCount;

    @Field(type = FieldType.Date)
    private Instant lastResent;
}
