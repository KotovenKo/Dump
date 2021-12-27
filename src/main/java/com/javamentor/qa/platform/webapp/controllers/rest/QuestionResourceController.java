package com.javamentor.qa.platform.webapp.controllers.rest;

import com.javamentor.qa.platform.service.abstracts.dto.QuestionDtoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/question/")
@Api(value = "Работа с вопросами", tags = {"Вопросы"})
public class QuestionResourceController {
    private final QuestionDtoService questionDtoService;

    @GetMapping("{id}")
    @ApiOperation(value = "Получение QuestionDto по Question id", tags = {"Получение QuestionDto"})
    @ApiResponses( value = {
            @ApiResponse(code = 200, message = "QuestionDto успешно получено"),
            @ApiResponse(code = 400, message = "Вопрос с таким ID не найден")
    })
    public ResponseEntity<?> getQuestionDtoById(@PathVariable Long id) {
        return questionDtoService.getQuestionDtoByQuestionId(id).isEmpty() ?
                new ResponseEntity<>("Question with id " + id + " not found!", HttpStatus.BAD_REQUEST) :
                new ResponseEntity<>(questionDtoService.getQuestionDtoByQuestionId(id), HttpStatus.OK);
    }
}
import com.javamentor.qa.platform.models.entity.question.Question;
import com.javamentor.qa.platform.models.entity.question.VoteQuestion;
import com.javamentor.qa.platform.models.entity.question.answer.VoteType;
import com.javamentor.qa.platform.models.entity.user.User;
import com.javamentor.qa.platform.models.entity.user.reputation.Reputation;
import com.javamentor.qa.platform.models.entity.user.reputation.ReputationType;
import com.javamentor.qa.platform.service.abstracts.model.QuestionService;
import com.javamentor.qa.platform.service.abstracts.model.ReputationService;
import com.javamentor.qa.platform.service.abstracts.model.UserService;
import com.javamentor.qa.platform.service.abstracts.model.VoteQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/user/{userId}/question/{questionId}") ///{userId} - удалить когда будет готова авторизация через JWT
public class QuestionResourceController {

    private final QuestionService questionService;
    private final ReputationService reputationService;
    private final VoteQuestionService voteQuestionService;
    private final UserService userService; //удалить когда будет готова авторизация через JWT

    @Autowired
    public QuestionResourceController(QuestionService questionService, ReputationService reputationService, VoteQuestionService voteQuestionService, UserService userService) {
        this.questionService = questionService;
        this.reputationService = reputationService;
        this.voteQuestionService = voteQuestionService;
        this.userService = userService; //удалить когда будет готова авторизация через JWT
    }

    //@PathVariable Long userId (когда будет готова авторизация через JWT) - заменить на @AuthenticationPrincipal User user
    @PostMapping("/upVote")
    public ResponseEntity<Integer> upVote(@PathVariable Long userId, @PathVariable Long questionId) {
        if (questionService.existsById(questionId)) {
            User user = userService.getById(userId).get();//удалить когда будет готова авторизация через JWT
            Question question = questionService.getById(questionId).get();
            List<VoteQuestion> voteQuestionList = voteQuestionService.getAllVoteQuestionsByQuestionId(questionId);
            if (voteQuestionService.userVoteCheck(questionId, user.getId())) {
                VoteQuestion voteQuestion = new VoteQuestion(user, question, LocalDateTime.now(), VoteType.UP_VOTE);
                voteQuestionList.add(voteQuestion);
                voteQuestionService.persist(voteQuestion);
                Reputation reputation = new Reputation(LocalDateTime.now(), question.getUser(), voteQuestion.getUser(), voteQuestion.getVote().getValue(), ReputationType.VoteQuestion, voteQuestion.getQuestion());
                reputationService.persist(reputation);
                return new ResponseEntity<>(voteQuestionList.size(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    //@PathVariable Long userId (когда будет готова авторизация через JWT) - заменить на @AuthenticationPrincipal User user
    @PostMapping("/downVote")
    public ResponseEntity<Integer> downVote(@PathVariable Long userId, @PathVariable Long questionId) {
        if (questionService.existsById(questionId)) {
            User user = userService.getById(userId).get();//удалить когда будет готова авторизация через JWT
            Question question = questionService.getById(questionId).get();
            List<VoteQuestion> voteQuestionList = voteQuestionService.getAllVoteQuestionsByQuestionId(questionId);
            if (voteQuestionService.userVoteCheck(questionId, user.getId())) {
                VoteQuestion voteQuestion = new VoteQuestion(user, question, LocalDateTime.now(), VoteType.DOWN_VOTE);
                voteQuestionList.add(voteQuestion);
                voteQuestionService.persist(voteQuestion);
                Reputation reputation = new Reputation(LocalDateTime.now(), question.getUser(), voteQuestion.getUser(), voteQuestion.getVote().getValue(), ReputationType.VoteQuestion, voteQuestion.getQuestion());
                reputationService.persist(reputation);
                return new ResponseEntity<>(voteQuestionList.size(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }
}

