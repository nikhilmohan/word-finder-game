package com.learningenglish.wordfinder.domain;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WordQuiz {
    private String id;
    private List<Word> words = new ArrayList<Word>();

}
