package org.gso.citations.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class Citation {

    public enum Status {
        PENDING,
        VALIDATED
    }

    @Id
    private String id;
    private String text;
    private String author; // The author of the quote

    private String submitterId;
    private LocalDateTime submissionDate;

    private String validatorId;
    private LocalDateTime validationDate;

    private Status status;
}
