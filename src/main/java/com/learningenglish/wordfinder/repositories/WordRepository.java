package com.learningenglish.wordfinder.repositories;

import com.learningenglish.wordfinder.domain.WordQuiz;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface WordRepository extends ReactiveMongoRepository<WordQuiz, String> {
}
