package com.learningenglish.wordfinder.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WordQuizAttempt {
    private String playerId;
    private List<WordAttempt> jumbledList = new ArrayList<WordAttempt>();
    private List<WordAttempt> missingLetterList = new ArrayList<WordAttempt>();
    private boolean invalidateAttempt = false;
}
