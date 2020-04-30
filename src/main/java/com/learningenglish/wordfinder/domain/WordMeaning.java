package com.learningenglish.wordfinder.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WordMeaning {
    private String word;
    private List<String> defs;
}
