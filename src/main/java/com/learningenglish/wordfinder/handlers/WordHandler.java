package com.learningenglish.wordfinder.handlers;

import com.learningenglish.wordfinder.domain.*;
import com.learningenglish.wordfinder.repositories.AttemptLogRepository;
import com.learningenglish.wordfinder.repositories.WordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Validator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Slf4j
public class WordHandler {

    WordRepository wordRepository;
    AttemptLogRepository attemptLogRepository;
    Validator validator;
    WebClient webClient = WebClient.create("https://api.datamuse.com/");
    private List<Category> categories = Arrays.asList(Category.values());
    public WordHandler(WordRepository wordRepository, AttemptLogRepository attemptLogRepository, Validator validator) {
        this.wordRepository = wordRepository;
        this.attemptLogRepository = attemptLogRepository;
        this.validator = validator;
    }

    public Mono <ServerResponse>  getWordQuiz(ServerRequest serverRequest) {
        WordQuiz wordQuiz = new WordQuiz();
        WordQuizResponse response = new WordQuizResponse();
        String topics = getRandomTopics(response);
        log.info("topics " + topics);

        return webClient.get().uri("words?" + topics + " &md=f&max=20")
                .retrieve()
                .bodyToFlux(WordMapping.class)
                .log("words from API")
                .filter(this::isEligible)
                .reduce(wordQuiz, (WordQuiz wq, WordMapping wordMapping) -> {
                    Word word = new Word();
                    word.setName(wordMapping.getWord());
                    word.setFrequency(wordMapping.getTags().stream().filter(s->s.startsWith("f:"))
                            .mapToDouble(s -> Double.parseDouble(s.replaceFirst("f:", "")))
                            .findAny().getAsDouble());

                    wq.getWords().add(word);
                    wq.getWords().sort(Comparator.comparing(Word::getFrequency).reversed());
                    return wq;
                }).log()
        .flatMap    ((WordQuiz quiz) -> {
            WordQuiz game = new WordQuiz();
            game.getWords().addAll(quiz.getWords().stream()
                    .limit(10).collect(Collectors.toList()));
            return wordRepository.save(game).log()
                    .flatMap(wordQuiz1 -> {
                        response.setId(wordQuiz1.getId());
                        response.getJumbledWords().addAll(buildJumbledWords(wordQuiz1
                                .getWords().stream().limit(5L)
                                .map(w -> w.getName())
                                .collect(Collectors.toList())));
                         response.getMissingLetterWords().addAll(
                                 buildMissingLetterWords(wordQuiz1.getWords().stream()
                                 .skip(5L)
                                 .map(w -> w.getName())
                                 .collect(Collectors.toList())));
                        return Mono.just(response);
                    }).flatMap(wordQuiz1 ->
                            ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(fromValue(wordQuiz1)));
        });

    }

    private List<String> buildMissingLetterWords(List<String> sourceList) {
        char dash = '_';
        List<String> missingLetterList = new ArrayList<String>();
        return sourceList.parallelStream()
        .map(w -> {
                   StringBuilder sb = new StringBuilder(w);
                   new Random().ints(w.length() / 2 - 1,
                            0, w.length() - 1)
                           .distinct()
                           .boxed()
                           .forEach(index -> sb.setCharAt(index, dash));
                   return sb.toString();
                }).collect(Collectors.toList());
    }

    private List<String> buildJumbledWords(List<String> sourceList) {
              List<String> jumbledList = new ArrayList<String>();

              return sourceList.stream()
                      .map(this::jumble)
                      .collect(Collectors.toList());
    }
    private String jumble(String s) {
        List<String> letters = Arrays.asList(s.split(""));
        Collections.shuffle(letters);
        StringBuilder sb = new StringBuilder(s.length());
        for (String c : letters) {
            sb.append(c);
        }
        return sb.toString();
    }

    private boolean isEligible(WordMapping word) {
        return !(word.getWord().contains(" ") || word.getWord().length() < 5);
    }

    private String getRandomTopics(WordQuizResponse response) {
        String topicString = "topics=";
        return topicString.concat(new Random().ints(3, 0, 14)
                .boxed()
                .map(i-> {
                    String topic = categories.get(i).getTopic();
                    response.getHints().add(topic);
                    return topic;
                })
                .reduce((topic1, topic2) ->  topic1 + "&" + topic2  ).get());

    }

    public Mono<ServerResponse> evaluateWordQuiz(ServerRequest serverRequest) {
        String quizId = serverRequest.pathVariable("id");
        Mono<WordQuizAttempt> wordQuizAttemptMono = serverRequest.bodyToMono(WordQuizAttempt.class);
        return wordQuizAttemptMono.flatMap(wordQuizAttempt -> {
            return wordRepository.findById(quizId)
                .flatMap(wordQuiz -> {
                    List<String> answers = wordQuiz.getWords().stream()
                            .map(Word::getName).collect(Collectors.toList());
                    wordQuizAttempt.getJumbledList().forEach(jumbledWordAttempt ->
                            jumbledWordAttempt.setResult(answers.contains(jumbledWordAttempt.getWord())));
                    wordQuizAttempt.getMissingLetterList().forEach(missingWordAttempt ->
                            missingWordAttempt.setResult(answers.contains(missingWordAttempt.getWord())));
                    return Mono.just(wordQuizAttempt);
                }).flatMap(wordQuizAttempt1 -> {
                    AttemptLog attemptLog = new AttemptLog();
                    attemptLog.setId(quizId);
                    attemptLog.setPlayerId(wordQuizAttempt1.getPlayerId());
                    attemptLog.setTimestamp(LocalDateTime.now());
                    applyScore(attemptLog, wordQuizAttempt1);
                    return attemptLogRepository.save(attemptLog).log();
                }).flatMap(attemptLog ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(wordQuizAttempt))
                );
        });

    }

    private void applyScore(AttemptLog attemptLog, WordQuizAttempt wordQuizAttempt1) {
        double jumbledScore = calculateResult(wordQuizAttempt1.getJumbledList());
        double missingLetterScore = calculateResult(wordQuizAttempt1.getMissingLetterList());
        double finalScore = (jumbledScore + missingLetterScore) / 2;
        attemptLog.setJumbledScore(jumbledScore);
        attemptLog.setMissingLetterScore(missingLetterScore);
        attemptLog.setFinalScore(finalScore);
    }
    private Double calculateResult(List<WordAttempt> answers) {
        double correctCount = answers.
                stream()
                .filter(answer -> answer.isResult())
                .count();
        log.info("Correct answer count " + correctCount);
        return 100.00d * (correctCount / (double)answers.size()) ;

    }

    public Mono<ServerResponse> getWordMeaning(ServerRequest serverRequest) {
        String word = serverRequest.pathVariable("word");

        return webClient.get().uri("words?sp=" + word + "&md=d&max=1")
                .retrieve()
                .bodyToFlux(WordMeaning.class)
                .log("words from API")
                .single()
                .flatMap(wordMeaning -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(wordMeaning)));

    }
}
