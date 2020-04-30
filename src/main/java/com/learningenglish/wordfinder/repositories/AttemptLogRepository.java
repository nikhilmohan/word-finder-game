package com.learningenglish.wordfinder.repositories;

import com.learningenglish.wordfinder.domain.AttemptLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AttemptLogRepository extends ReactiveMongoRepository<AttemptLog, String> {
}
