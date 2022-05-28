package com.javamentor.qa.platform.service.impl;

import com.javamentor.qa.platform.models.entity.BookMarks;
import com.javamentor.qa.platform.models.entity.question.IgnoredTag;
import com.javamentor.qa.platform.models.entity.question.Question;
import com.javamentor.qa.platform.models.entity.question.Tag;
import com.javamentor.qa.platform.models.entity.question.TrackedTag;
import com.javamentor.qa.platform.models.entity.question.answer.Answer;
import com.javamentor.qa.platform.models.entity.user.Role;
import com.javamentor.qa.platform.models.entity.user.User;
import com.javamentor.qa.platform.models.entity.user.reputation.Reputation;
import com.javamentor.qa.platform.models.entity.user.reputation.ReputationType;
import com.javamentor.qa.platform.service.abstracts.model.*;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class TestDataInitService {
    private final RoleService roleService;
    private final UserService userService;
    private final ReputationService reputationService;
    private final Flyway flyway;
    private final AnswerService answerService;
    private final QuestionService questionService;
    private final BookmarkService bookmarkService;
    private final TagService tagService;
    private final TrackedTagService trackedTagService;
    private final IgnoredTagService ignoredTagService;

    @Autowired
    public TestDataInitService(RoleService roleService, UserService userService, ReputationService reputationService, Flyway flyway,
                               AnswerService answerService, QuestionService questionService,
                               TagService tagService, TrackedTagService trackedTagService,
                               IgnoredTagService ignoredTagService, BookmarkService bookmarkService) {
        this.roleService = roleService;
        this.userService = userService;
        this.reputationService = reputationService;
        this.flyway = flyway;
        this.answerService = answerService;
        this.questionService = questionService;
        this.tagService = tagService;
        this.trackedTagService = trackedTagService;
        this.ignoredTagService = ignoredTagService;
        this.bookmarkService = bookmarkService;
    }

    public void init() {
        addRole();
        addUser();
        addTag();
        addQuestion();
        addAnswer();
        addTrackedAndIgnoredTag();
        addBookmark();
        addReputation();
    }

    private void addRole() {
        // изменить при необходимости
        roleService.persist(new Role("ADMIN"));
        roleService.persist(new Role("USER"));
    }

    private void addTag() {
        StringBuilder name = new StringBuilder();
        StringBuilder description = new StringBuilder();

        for (int x = 1; x <= 50; x++) {
            name.delete(0, name.length()).append("tag:").append(x);
            description.delete(0, description.length()).append("this is tag by name: ").append(name);
            Tag tag = new Tag();
            tag.setName(name.toString());
            tag.setDescription(description.toString());
            tagService.persist(tag);
            name.delete(0, name.length());
            description.delete(0, description.length());
        }
    }

    private void addQuestion() {
        StringBuilder title = new StringBuilder();
        StringBuilder description = new StringBuilder();
        List<Tag> tags = new ArrayList<>();

        for (int x = 1; x <= 50; x++) {
            tags.clear();
            title.delete(0, 50).append("title:").append(x);
            description.delete(0, 50).append("this is question by title: ").append(title);

            // добавление рандомных тегов в рандомном количестве [1;5]
            for (int y = (1 + (int) (Math.random() * 4)); y <= 5; y++) {
                Tag tag = tagService.getById((long) (1 + (int) (Math.random() * 49))).get();
                if (!tags.contains(tag)) {
                    tags.add(tag);
                }
            }

            Question question = new Question();
            question.setTitle(title.toString());
            question.setDescription(description.toString());
            question.setTags(tags);
            // добавление рандомного юзера
            question.setUser(userService.getById((long) (1 + (int) (Math.random() * 49))).get());
            questionService.persist(question);
        }
    }

    private void addAnswer() {
        StringBuilder htmlBody = new StringBuilder();
        Random random = new Random();
        // добавление рандомного количества ответов [1;50]
        for (int x = 1 + (int) (Math.random() * 49); x <= 50; x++) {
            htmlBody.delete(0, htmlBody.length()).append("htmlBody:").append(x);
            Answer answer = new Answer();
            answer.setIsDeleted(random.nextBoolean());
            answer.setHtmlBody(htmlBody.toString());
            answer.setIsHelpful(random.nextBoolean());
            answer.setIsDeletedByModerator(random.nextBoolean());
            answer.setQuestion(questionService.getById((long) (1 + (int) (Math.random() * 49))).get());
            answer.setUser(userService.getById((long) (1 + (int) (Math.random() * 49))).get());
            //добавдение модератора к рамдомной части ответов
            for (int y = 1 + (int) (Math.random() * 49); y <= x; y = y + 2) {
                if (y == x) {
                    answer.setEditModerator(userService.getAll().stream()
                            .filter(user -> user.getRole().getName().equals("ADMIN")).findAny().get());
                    break;
                }
            }
            answerService.persist(answer);
        }
    }

    private void addUser() {
        // изменить при необходимости
        Role adminRole = roleService.getById(1L).get();
        Role userRole = roleService.getById(2L).get();

        StringBuilder email = new StringBuilder();
        StringBuilder password = new StringBuilder();
        StringBuilder fullName = new StringBuilder();
        StringBuilder city = new StringBuilder();
        StringBuilder linkSite = new StringBuilder();
        StringBuilder linkGitHub = new StringBuilder();
        StringBuilder linkVk = new StringBuilder();
        StringBuilder about = new StringBuilder();
        StringBuilder imageLink = new StringBuilder();
        StringBuilder nickname = new StringBuilder();

        User admin = new User();
        admin.setRole(userRole);
        admin.setEmail(email.append("vasya@mail.ru").toString());
        admin.setPassword(password.append("password").toString());
        admin.setFullName(fullName.append("Vasya").toString());
        admin.setCity(city.append("Vasya's City").toString());
        admin.setLinkSite(linkSite.append("vasya.ru").toString());
        admin.setLinkGitHub(linkGitHub.append("github.com/vasya").toString());
        admin.setLinkVk(linkVk.append("vk.com/vasya").toString());
        admin.setAbout(about.append("Hello my name is Vasya").toString());
        admin.setImageLink(imageLink.append("vasya.ru/myphoto/1").toString());
        admin.setNickname(nickname.append("Vasya").toString());
        userService.persist(admin);

        User admin1 = new User();
        admin1.setRole(adminRole);
        admin1.setEmail(email.delete(0, email.length()).append("volodya@mail.ru").toString());
        admin1.setPassword(password.delete(0, password.length()).append("123").toString());
        admin1.setFullName(fullName.delete(0, fullName.length()).append("Volodya").toString());
        admin1.setCity(city.delete(0, city.length()).append("Volodya's City").toString());
        admin1.setLinkSite(linkSite.delete(0, linkSite.length()).append("volodya.ru").toString());
        admin1.setLinkGitHub(linkGitHub.delete(0, linkGitHub.length()).append("github.com/volodya").toString());
        admin1.setLinkVk(linkVk.delete(0, linkVk.length()).append("github.com/volodya").toString());
        admin1.setAbout(about.delete(0, about.length()).append("Hello my name is Volodya").toString());
        admin1.setImageLink(imageLink.delete(0, imageLink.length()).append("volodya.ru/myphoto/1").toString());
        admin1.setNickname(nickname.delete(0, nickname.length()).append("Vova").toString());
        userService.persist(admin1);

        for (int x = 3; x <= 50; x++) {

            email.delete(0, email.length()).append(x).append("user@mail.ru");
            password.delete(0, password.length()).append(x).append(111);
            fullName.delete(0, fullName.length()).append(x).append("user");
            city.delete(0, city.length()).append(x).append("user's City");
            linkSite.delete(0, linkSite.length()).append(x).append("user.ru");
            linkGitHub.delete(0, linkGitHub.length()).append("github.com/").append(x).append("user");
            linkVk.delete(0, linkVk.length()).append("vk.com/").append(x).append("user");
            about.delete(0, about.length()).append("Hello my name is ").append(x).append("user");
            imageLink.delete(0, imageLink.length()).append(x).append("user").append(".ru/myphoto/1");
            nickname.delete(0, nickname.length()).append(x).append("user");

            User u = new User();
            u.setRole(userRole);
            u.setEmail(email.toString());
            u.setPassword(password.toString());
            u.setFullName(fullName.toString());
            u.setCity(city.toString());
            u.setLinkSite(linkSite.toString());
            u.setLinkGitHub(linkGitHub.toString());
            u.setLinkVk(linkVk.toString());
            u.setAbout(about.toString());
            u.setImageLink(imageLink.toString());
            u.setNickname(nickname.toString());

            userService.persist(u);
        }
    }

    //Инициализация Репутации
    public void addReputation() {

        for (int i = 2; i <= 50; i++) {
            Reputation reputation = new Reputation();
            reputation.setPersistDate(LocalDateTime.now());
            reputation.setAuthor(userService.getById((long) (1 + (int) (Math.random() * 49))).get());
            reputation.setSender(userService.getById((long) (1 + (int) (Math.random() * 49))).get());
            reputation.setCount((int) (Math.random() * 40));
            reputation.setType(ReputationType.Answer);
//            reputation.setType((ReputationType) Arrays.asList(ReputationType.values()).get(i));
            reputation.setQuestion(questionService.getById((long) (1 + (int) (Math.random() * 49))).get());

//           reputation.setAnswer(answerService.getById((long) (1 + (int) (Math.random() * 49))).get().);


        }
    }

    private void addBookmark() {
        List<BookMarks> bookmarks = new ArrayList<>();

        for (int x = 2; x <= 50; x++) {
            bookmarks.clear();
            int countbookmarks = (int) (Math.random() * 4);
            User user = userService.getById((long) x).get();

            for (int y = 0; y <= 5; y++) {
                BookMarks bookmark = new BookMarks();
                Question question = questionService.getById((long) (1 + (int) (Math.random() * 49))).get();

                if (!bookmarks.contains(question) && bookmarkService.bookmarkByUserId(user.getId()).size() < countbookmarks) {
                    bookmarks.add(bookmark);
                    bookmark.setQuestion(question);
                    bookmark.setUser(user);
                    bookmarkService.persist(bookmark);
                }
            }
        }
    }


    private void addTrackedAndIgnoredTag() {
        List<Tag> tags = new ArrayList<>();

        for (int x = 2; x <= 50; x++) {
            tags.clear();
            int countTrackedTag = (int) (Math.random() * 4);
            int countIgnoredTag = (int) (Math.random() * 4);
            User user = userService.getById((long) x).get();

            for (int y = 0; y <= 5; y++) {
                TrackedTag trackedTag = new TrackedTag();
                IgnoredTag ignoredTag = new IgnoredTag();
                Tag tag = tagService.getById((long) (1 + (int) (Math.random() * 49))).get();
                if (!tags.contains(tag) && trackedTagService.trackedTagsByUserId(user.getId()).size() < countTrackedTag) {
                    tags.add(tag);
                    trackedTag.setTrackedTag(tag);
                    trackedTag.setUser(user);
                    trackedTagService.persist(trackedTag);
                } else if (!tags.contains(tag) && ignoredTagService.ignoredTagsByUserId(user.getId()).size() < countIgnoredTag) {
                    tags.add(tag);
                    ignoredTag.setIgnoredTag(tag);
                    ignoredTag.setUser(user);
                    ignoredTagService.persist(ignoredTag);
                }
            }
        }
    }
}