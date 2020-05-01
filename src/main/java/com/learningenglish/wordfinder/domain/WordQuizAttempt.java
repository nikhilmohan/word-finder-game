package com.learningenglish.wordfinder.domain;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WordQuizAttempt {
    @NotEmpty
    private String playerId;
    @NotEmpty
    private List<WordAttempt> jumbledList = new ArrayList<WordAttempt>();
    @NotEmpty
    private List<WordAttempt> missingLetterList = new ArrayList<WordAttempt>();
    private boolean invalidateAttempt = false;
}
