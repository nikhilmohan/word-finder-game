package com.learningenglish.wordfinder.handlers;

import com.learningenglish.wordfinder.domain.*;
import com.learningenglish.wordfinder.repositories.WordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@SpringBootTest
@AutoConfigureWebTestClient
class WordHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    WordRepository wordRepository;


    @Test
    void getWordQuiz() {
       WordQuizResponse quiz = webTestClient.get().uri("http://localhost:9020/word-finder/api/word-game")
                .exchange()
                .expectBody(WordQuizResponse.class)
                .returnResult()
                .getResponseBody();

       assertEquals(5, quiz.getJumbledWords().size());
       assertEquals(5, quiz.getMissingLetterWords().size());
       assertNotNull(quiz.getId());
       assertEquals(3, quiz.getHints().size());

    }

    @Test
    void evaluateWordQuiz() {
        WordQuiz wordQuiz = new WordQuiz();
        wordQuiz.getWords().addAll(Arrays.asList(new Word("apple", 0.4),
                new Word("mango", 0.5),
                new Word("lemon", 0.3),
        new Word("orange", 0.7)));
        wordQuiz.setId("abc");
        wordRepository.save(wordQuiz).block();

        WordQuizAttempt wordQuizAttempt = new WordQuizAttempt();
        WordAttempt w1= new WordAttempt("apple", false);
        WordAttempt w2= new WordAttempt("mango", false);
        WordAttempt w3= new WordAttempt("lemon", false);
        WordAttempt w4= new WordAttempt("orange", false);


        wordQuizAttempt.getMissingLetterList().addAll(Arrays.asList(w1, w2));
        wordQuizAttempt.getJumbledList().addAll(Arrays.asList(w3, w4));
        wordQuizAttempt.setPlayerId("new guy");



        WordQuizAttempt attemptResponse = webTestClient.post().uri("http://localhost:9020/word-finder/api/word-game/abc")
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(wordQuizAttempt))
                .exchange()
                .expectBody(WordQuizAttempt.class)
                .returnResult()
                .getResponseBody();

        assertEquals(attemptResponse.getPlayerId(), "new guy");
        assertEquals(attemptResponse.getJumbledList().stream().filter(WordAttempt::isResult).count(), 2);
        assertEquals(attemptResponse.getMissingLetterList().stream().filter(WordAttempt::isResult).count(), 2);

    }

    @Test
    void getWordMeaning() {
        WordMeaning meaning = webTestClient.get().uri("http://localhost:9020/word-finder/api/word-game/chair")
                .exchange()
                .expectBody(WordMeaning.class)
                .returnResult()
                .getResponseBody();
        assertEquals("chair", meaning.getWord());
        assertNotNull(meaning.getDefs());
    }
}