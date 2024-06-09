package org.profitsoft.photomessagebroker.database.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@NoArgsConstructor
public class EmailSender {
    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Text)
    private String name;
}
