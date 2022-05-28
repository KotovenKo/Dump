package com.javamentor.qa.platform.webapp.controllers.rest;

import com.javamentor.qa.platform.exception.ApiRequestException;
import com.javamentor.qa.platform.models.dto.PageDto;
import com.javamentor.qa.platform.models.dto.QuestionCreateDto;
import com.javamentor.qa.platform.models.dto.QuestionViewDto;
import com.javamentor.qa.platform.models.entity.BookMarks;
import com.javamentor.qa.platform.models.entity.Comment;
import com.javamentor.qa.platform.models.entity.question.CommentQuestion;
import com.javamentor.qa.platform.models.entity.question.Question;
import com.javamentor.qa.platform.models.entity.question.QuestionViewed;
import com.javamentor.qa.platform.models.entity.question.VoteQuestion;
import com.javamentor.qa.platform.models.entity.question.answer.VoteType;
import com.javamentor.qa.platform.models.entity.user.User;
import com.javamentor.qa.platform.service.abstracts.dto.QuestionDtoService;
import com.javamentor.qa.platform.service.abstracts.model.CommentQuestionService;
import com.javamentor.qa.platform.service.abstracts.model.QuestionService;
import com.javamentor.qa.platform.service.abstracts.model.TagService;
import com.javamentor.qa.platform.service.abstracts.model.VoteQuestionService;
import com.javamentor.qa.platform.service.abstracts.model.QuestionViewedService;
import com.javamentor.qa.platform.service.abstracts.model.BookmarkService;
import com.javamentor.qa.platform.webapp.converters.QuestionConverter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/question")
@Api(value = "Работа с вопросами", tags = {"Вопросы"})
public class QuestionResourceController {
    private final QuestionDtoService questionDtoService;
    private final QuestionService questionService;
    private final VoteQuestionService voteQuestionService;
    private final TagService tagService;
    private final QuestionConverter questionConverter;
    private final QuestionViewedService questionViewedService;
    private final BookmarkService bookmarkService;
    private final CommentQuestionService commentQuestionService;

    @GetMapping("/{id}")
    @ApiOperation(value = "Получение QuestionDto по Question id", tags = {"Получение QuestionDto"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "QuestionDto успешно получено"),
            @ApiResponse(code = 400, message = "Вопрос с таким ID не найден")
    })
    public ResponseEntity<?> getQuestionDtoById(@PathVariable Long id) {
        return questionDtoService.getQuestionDtoByQuestionId(id).isEmpty() ?
                ResponseEntity.badRequest().body("Question with id " + id + " not found!") :
                ResponseEntity.ok().body(questionDtoService.getQuestionDtoByQuestionId(id));

    }

    @GetMapping("/{id}/comment")
    @ApiOperation(value = "Получение списка QuestionCommentDto по Question id",
            tags = {"список", "комментарий", "вопрос"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Список QuestionCommentDto успешно получен"),
            @ApiResponse(code = 400, message = "Вопрос с таким ID не найден")
    })
    public ResponseEntity<?> getQuestionCommentById(@PathVariable Long id) {
        return questionService.getById(id).isPresent() ?
                ResponseEntity.ok().body(questionDtoService.getQuestionCommentByQuestionId(id)) :
                ResponseEntity.badRequest().body("Question with id " + id + " not found!");
    }

    @GetMapping("/noAnswer")
    @ApiOperation(value = "Получение QuestionDto, на которые нет ответов", tags = {"Получение QuestionDto"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "QuestionDto успешно получено"),
            @ApiResponse(code = 400, message = "Параметры заданы неверно")
    })
    public ResponseEntity<?> getQuestionDtoNoAnswer(@RequestParam int page,
                                                    @RequestParam(defaultValue = "10") int items,
                                                    @RequestParam(required = false, defaultValue = "0") List<Long> ignoredTags,
                                                    @RequestParam(required = false) List<Long> trackedTags) {
        Map<String, Object> map = new HashMap<>();
        map.put("class", "QuestionDtoNoAnswer");
        map.put("currentPageNumber", page);
        map.put("itemsOnPage", items);
        map.put("ignoredTags", ignoredTags);
        map.put("trackedTags", trackedTags);
        PageDto<QuestionViewDto> pageDto = questionDtoService.getPageDto(page, items, map);
        return ResponseEntity.ok().body(pageDto);
    }

    @PostMapping()
    @ApiOperation(value = "Add a new question", tags = {"Question"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful question creation"),
            @ApiResponse(code = 400, message = "Validation error")
    })
    public ResponseEntity<?> createQuestion(@Validated @RequestBody QuestionCreateDto questionCreateDto) {
        Question question = questionConverter.questionCreateDtoToQuestion(questionCreateDto);
        question.setUser((User) SecurityContextHolder.getContext().getAuthentication().getDetails());
        questionService.persist(question);
        return ResponseEntity.ok().body(questionDtoService.getQuestionDtoByQuestionId(question.getId()));
    }

    @PostMapping("{questionId}/upVote")
    @ApiOperation(value = "Голосование за Question по Question id", tags = {"VoteQuestion up"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешное голосование"),
            @ApiResponse(code = 400, message = "Вопрос с таким ID не найден или Вы уже голосовали за данный вопрос")
    })
    public ResponseEntity<?> upVote(@PathVariable Long questionId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
        Optional<Question> optionalQuestion = questionService.getById(questionId);
        /*
         * Проверка наличия голоса на вопросе от авторизированного юзера в соответствии с тз сущности
         */
        Optional<VoteQuestion> voteQuestionOptional = voteQuestionService.getByUserIdAndQuestionId(user.getId(), questionId);
        if (voteQuestionOptional.isPresent()) {
            return ResponseEntity.badRequest().body("You allready voted for the question with id " + questionId);
        }

        if (optionalQuestion.isEmpty()) {
            return ResponseEntity.badRequest().body("Question with id " + questionId + " not found");
        }
        Question question = optionalQuestion.get();
        if (!(question.getUser().getId()).equals(user.getId())) {
            VoteQuestion voteQuestion = new VoteQuestion(user, question, LocalDateTime.now(), VoteType.UP_VOTE);
            voteQuestionService.persist(voteQuestion);
            return ResponseEntity.ok().body(voteQuestionService.getTotalVoteQuestionsByQuestionId(questionId));
        }
        return ResponseEntity.badRequest().body("Voting for your question with id " + questionId + " not allowed");

    }

    @PostMapping("/{questionId}/view")
    @ApiOperation(value = "Просмотр Question авторизированным пользователем", tags = {"QuestionViewed check or add"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Просмотр успешно произведен"),
            @ApiResponse(code = 400, message = "Вопрос с таким ID не найден")
    })
    public ResponseEntity<?> viewQuestion(@PathVariable Long questionId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
        Optional<Question> optionalQuestion = questionService.getById(questionId);
        if (optionalQuestion.isPresent()) {
            Question question = optionalQuestion.get();
            if (!questionViewedService.questionViewCheckByUserIdAndQuestionId(questionId, user.getEmail())) {
                QuestionViewed questionViewed = new QuestionViewed();
                questionViewed.setQuestion(question);
                questionViewed.setUser(user);
                questionViewedService.persist(questionViewed);
                return ResponseEntity.ok().body("Просмотр вопроса с id:  " + questionId + " успешно произведен");
            }
            return ResponseEntity.ok().body("Просмотр вопроса с id:  " + questionId + " уже существует");
        }
        return ResponseEntity.badRequest().body("Вопрос с id:  " + questionId + " не найден");
    }

    @PostMapping("/{questionId}/downVote")
    @ApiOperation(value = "Голосование за Question по Question id", tags = {"VoteQuestion down"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешное голосование"),
            @ApiResponse(code = 400, message = "Вопрос с таким ID не найден или Вы уже голосовали за данный вопрос  ")
    })
    public ResponseEntity<?> downVote(@PathVariable Long questionId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
        Optional<Question> optionalQuestion = questionService.getById(questionId);

        /*
         * Проверка наличия голоса на вопросе от авторизированного юзера в соответствии с тз сущности
         */
        Optional<VoteQuestion> voteQuestionOptional = voteQuestionService.getByUserIdAndQuestionId(user.getId(), questionId);
        if (voteQuestionOptional.isPresent()) {
            return ResponseEntity.badRequest().body("You allready voted for the question with id " + questionId);
        }

        if (optionalQuestion.isEmpty()) {
            return ResponseEntity.badRequest().body("Question with id " + questionId + " not found");
        }
            Question question = optionalQuestion.get();
            if (!(question.getUser().getId()).equals(user.getId())) {
                VoteQuestion voteQuestion = new VoteQuestion(user, question, LocalDateTime.now(), VoteType.DOWN_VOTE);
                voteQuestionService.persist(voteQuestion);
                return ResponseEntity.ok().body(voteQuestionService.getTotalVoteQuestionsByQuestionId(questionId));
            }
        return ResponseEntity.badRequest().body("Voting for your question with id " + questionId + " not allowed");
    }

    @GetMapping
    @ApiOperation(value = "Получение всех QuestionDto с пагинацией", tags = {"Get All QuestionDto"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Все QuestionDto получены"),
            @ApiResponse(code = 400, message = "QuestionDto не найдены")
    })
    public ResponseEntity<PageDto<QuestionViewDto>> getAllQuestionDto(@RequestParam int currentPageNumber,
                                                                      @RequestParam(defaultValue = "10") int itemsOnPage,
                                                                      @RequestParam(required = false) List<Long> trackedTags,
                                                                      @RequestParam(required = false) List<Long> ignoredTags) {
        Map<String, Object> paginationMap = new HashMap<>();
        paginationMap.put("class", "AllQuestionDto");
        paginationMap.put("currentPageNumber", currentPageNumber);
        paginationMap.put("itemsOnPage", itemsOnPage);
        paginationMap.put("trackedTags", trackedTags);
        paginationMap.put("ignoredTags", ignoredTags);

        return ResponseEntity.ok(questionDtoService.getPageDto(currentPageNumber, itemsOnPage, paginationMap));
    }

    @GetMapping("/sorted")
    @ApiOperation(value = "Получение всех QuestionDto с пагинацией и сортировкой по голосам, ответам и просмотрам",
            tags = {"Get All Sorted QuestionDto"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Все QuestionDto получены"),
            @ApiResponse(code = 400, message = "QuestionDto не найдены")
    })
    public ResponseEntity<PageDto<QuestionViewDto>> getAllSortedQuestionDto(@RequestParam int currentPageNumber,
                                                                            @RequestParam(defaultValue = "10") int itemsOnPage) {
        Map<String, Object> paginationMap = new HashMap<>();
        paginationMap.put("class", "AllQuestionDtoByVoitAndAnswerAndView");
        paginationMap.put("currentPageNumber", currentPageNumber);
        paginationMap.put("itemsOnPage", itemsOnPage);
        return ResponseEntity.ok(questionDtoService.getPageDto(currentPageNumber, itemsOnPage, paginationMap));
    }

    @GetMapping("tag/{id}")
    @ApiOperation(value = "Получение QuestionDto по TagId", tags = {"Получение QuestionDto по tagId"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "QuestionDto успешно получено"),
            @ApiResponse(code = 400, message = "Данный TagId не найден")
    })
    public ResponseEntity<?> getQuestionDtoByTagId(@PathVariable Long id,
                                                   @RequestParam int page,
                                                   @RequestParam(defaultValue = "10") int items) {
        if (tagService.existsById(id)) {
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("class", "AllQuestionDtoByTagId");
            objectMap.put("tagId", id);
            objectMap.put("currentPageNumber", page);
            objectMap.put("itemsOnPage", items);
            PageDto<QuestionViewDto> pageDto = questionDtoService.getPageDto(page, items, objectMap);
            return ResponseEntity.ok().body(pageDto);
        }
        return ResponseEntity.badRequest().body("TagId " + id + " not found");
    }

    @GetMapping("/count")
    @ApiOperation(value = "Получение количества вопросов в Question", tags = {"QuestionCount"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Количество вопросов успешно получено")
    })
    public ResponseEntity<Integer> getCountQuestion() {
        return ResponseEntity.ok().body(questionService.getCountQuestion());
    }

    @GetMapping("/new")
    @ApiOperation(value = "Получение QuestionDto отсортированных по дате",
            tags = {"Получение QuestionDto отсортированных по дате"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "QuestionDto успешно получено"),
            @ApiResponse(code = 400, message = "Неправильный запрос"),
            @ApiResponse(code = 500, message = "Внутренняя ошибка")
    })
    public ResponseEntity<?> getQuestionSortedByDate(@RequestParam int page, @RequestParam(defaultValue = "10") int items,
                                                     @RequestParam(required = false) List<Long> trackedTags,
                                                     @RequestParam(required = false, defaultValue = "0") List<Long> ignoredTags) {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("class", "AllQuestionDtoSortedByDate");
        objectMap.put("currentPageNumber", page);
        objectMap.put("itemsOnPage", items);
        objectMap.put("tracked", trackedTags);
        objectMap.put("ignored", ignoredTags);
        PageDto<QuestionViewDto> pageDto = questionDtoService.getPageDto(page, items, objectMap);
        return ResponseEntity.ok().body(pageDto);
    }

    @GetMapping("/sortedByWeek")
    @ApiOperation(value = "Получение всех QuestionDto с пагинацией и сортировкой по голосам, ответам и просмотрам за неделю",
            tags = {"Get Sorted by Week QuestionDto"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Все QuestionDto получены"),
            @ApiResponse(code = 400, message = "QuestionDto не найдены")
    })
    public ResponseEntity<PageDto<QuestionViewDto>> getAllQuestionsByVoteAndAnswerByWeek(@RequestParam int currentPageNumber,
                                                                                         @RequestParam(defaultValue = "10") int itemsOnPage,
                                                                                         @RequestParam(required = false) List<Long> trackedTags,
                                                                                         @RequestParam(required = false, defaultValue = "0") List<Long> ignoredTags) {
        Map<String, Object> paginationMap = new HashMap<>();
        paginationMap.put("class", "AllQuestionsByVoteAndAnswerByWeek");
        paginationMap.put("currentPageNumber", currentPageNumber);
        paginationMap.put("itemsOnPage", itemsOnPage);
        paginationMap.put("tracked", trackedTags);
        paginationMap.put("ignored", ignoredTags);

        return ResponseEntity.ok(questionDtoService.getPageDto(currentPageNumber, itemsOnPage, paginationMap));
    }


    @GetMapping("/sortedByMonth")
    @ApiOperation(value = "Получение QuestionDto с пагинацией и сортировкой по голосам, ответам и просмотрам за месяц",
            tags = {"Get Month Sorted QuestionDto"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "QuestionDto получены"),
            @ApiResponse(code = 400, message = "QuestionDto не найдены")
    })
    public ResponseEntity<PageDto<QuestionViewDto>> getMonthSortedQuestionDto(@RequestParam int currentPageNumber,
                                                                              @RequestParam(defaultValue = "10") int itemsOnPage,
                                                                              @RequestParam(required = false) List<Long> trackedTags,
                                                                              @RequestParam(required = false, defaultValue = "0") List<Long> ignoredTags) {
        Map<String, Object> paginationMap = new HashMap<>();
        paginationMap.put("class", "AllQuestionsDtoByVoteAndAnswerAndViewByMonth");
        paginationMap.put("currentPageNumber", currentPageNumber);
        paginationMap.put("itemsOnPage", itemsOnPage);
        paginationMap.put("trackedTags", trackedTags);
        paginationMap.put("ignoredTags", ignoredTags);

        return ResponseEntity.ok(questionDtoService.getPageDto(currentPageNumber, itemsOnPage, paginationMap));
    }

    @PostMapping("/{questionId}/bookmark")
    @ApiOperation(value = "Add a new bookmark for authorized user", tags = {"Bookmarks"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Закладка успешно создана"),
            @ApiResponse(code = 400, message = "При добавлении закладки что-то пошло не так")

    })
    public ResponseEntity<?> createBookmark(@PathVariable Long questionId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
        Optional<Question> optionalQuestion = questionService.getById(questionId);
        BookMarks bookmark = new BookMarks();
        if (optionalQuestion.isPresent()) {
            bookmark.setQuestion(optionalQuestion.orElseThrow());
            bookmark.setUser(user);
            bookmarkService.persist(bookmark);
            return ResponseEntity.ok().body("Вопрос с Id:" + questionId + " для пользователя с Id:" +
                    user.getId() + " добавлен в закладки");
        }
        return ResponseEntity.badRequest().body("Закладка не добавлена, вопроса ID " + questionId + " не существует");
    }

    @PostMapping("/{id}/comment")
    @ApiOperation(value = "Добавление комментария в вопрос", tags = {"Add comment to question"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Комментарий успешно добавлен"),
            @ApiResponse(code = 400, message = "Вопрос с таким ID не найден")
    })
    public ResponseEntity<?> addQuestionToComment(@PathVariable Long id,
                                                        @RequestBody String commentString) {
        Optional<Question> questionOptional = questionService.getById(id);
        if (questionOptional.isPresent()) {
            Question question = questionOptional.get();
            CommentQuestion commentQuestion = new CommentQuestion();
            commentQuestion.setText(commentString);
            commentQuestion.setQuestion(question);
            commentQuestionService.persist(commentQuestion);
        }
        return ResponseEntity.badRequest().body("Комментарий не был добавлен к вопросу так как вопрос с Id:" + id + "был не найден");
    }

}