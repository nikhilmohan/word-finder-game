package com.learningenglish.wordfinder.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Getter
@Setter
@ToString
public class AttemptLog {
    @Id
    private String id;
    private String playerId;
    private LocalDateTime timestamp;
    private double finalScore;
    private double jumbledScore;
    private double missingLetterScore;

}
