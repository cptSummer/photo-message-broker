package org.profitsoft.photomessagebroker.database.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


@Getter
@Setter
@NoArgsConstructor
public class ErrorMessage {
    @Field(type = FieldType.Text)
    private String message;

    @Field(type = FieldType.Text)
    private String errorClass;
}
