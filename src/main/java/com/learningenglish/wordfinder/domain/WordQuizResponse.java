package com.learningenglish.wordfinder.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WordQuizResponse {
    private String id;
    private List<String> jumbledWords = new ArrayList<String>();
    private List<String> missingLetterWords = new ArrayList<String>();
    private List<String> hints = new ArrayList<String>();
}
