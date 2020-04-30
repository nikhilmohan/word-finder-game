package com.learningenglish.wordfinder.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WordMapping {
    private String word;
    private List<String> tags = new ArrayList<String>();
}
