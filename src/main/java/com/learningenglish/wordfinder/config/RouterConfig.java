package com.learningenglish.wordfinder.config;


import com.learningenglish.wordfinder.domain.Word;
import com.learningenglish.wordfinder.handlers.WordHandler;
import com.learningenglish.wordfinder.repositories.AttemptLogRepository;
import com.learningenglish.wordfinder.repositories.WordRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import javax.validation.Validator;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> itemRoutes(WordHandler wordHandler) {
        return RouterFunctions.route(GET("/word-finder/api/word-game").and(accept(MediaType.APPLICATION_JSON)), wordHandler::getWordQuiz)
                .andRoute(POST("/word-finder/api/word-game/{id}").and(accept(MediaType.APPLICATION_JSON)), wordHandler::evaluateWordQuiz)
                .andRoute(GET("/word-finder/api/word-game/{word}").and(accept(MediaType.APPLICATION_JSON)), wordHandler::getWordMeaning);
    }

    @Bean
    public WordHandler wordHandler(WordRepository wordRepository, AttemptLogRepository attemptLogRepository, Validator validator)    {
        return new WordHandler(wordRepository, attemptLogRepository, validator);
    }
    /*
    @Bean
    public Dataloader dataloader(QuizRepository quizRepository)    {
        return new Dataloader(quizRepository);
    }

     */

}
