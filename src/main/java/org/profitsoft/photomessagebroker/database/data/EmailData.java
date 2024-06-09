package org.profitsoft.photomessagebroker.database.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;


@Getter
@Setter
@Document(indexName = "notifications")
public class EmailData {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String subject;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Object)
    private EmailSender from;

    @Field(type = FieldType.Object)
    private EmailRecipient recipients;

    @Field(type = FieldType.Object)
    private EmailError error;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Date)
    private Instant timeSent;

    @Field(type = FieldType.Long)
    private Long delayTime;

}

