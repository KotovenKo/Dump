package com.javamentor.qa.platform.api;

import com.github.database.rider.core.api.dataset.DataSet;
import com.javamentor.qa.platform.AbstractApiTest;
import com.javamentor.qa.platform.models.dto.AnswerBodyDto;
import com.javamentor.qa.platform.models.entity.question.answer.Answer;
import com.javamentor.qa.platform.models.entity.question.answer.CommentAnswer;
import com.javamentor.qa.platform.models.entity.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TestAnswerResourceController extends AbstractApiTest {
    @Test
    @DataSet(value = {
            "datasets/AnswerResourceController/deleteAnswerById/answer.yml",
            "datasets/AnswerResourceController/deleteAnswerById/tag.yml",
            "datasets/AnswerResourceController/deleteAnswerById/user.yml",
            "datasets/AnswerResourceController/deleteAnswerById/role.yml",
            "datasets/AnswerResourceController/deleteAnswerById/question.yml",
            "datasets/AnswerResourceController/deleteAnswerById/questionHasTag.yml",
            "datasets/AnswerResourceController/deleteAnswerById/reputation.yml",
            "datasets/AnswerResourceController/deleteAnswerById/voteAnswer.yml"
    })
    void deleteAnswerById() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.delete("/api/user/question/100/answer/100")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isOk());

        //Проверяем, что ответа с id=100 в базе не существует
        Assertions.assertFalse(((long) em.createQuery("SELECT COUNT(e) FROM " + Answer.class.getName() +
                        " e WHERE e.id = 100")
                .getSingleResult()) > 0);
    }

    @Test
    @DataSet(value = {
            "datasets/AnswerResourceController/tryToDeleteNonExistedId/answer.yml",
            "datasets/AnswerResourceController/tryToDeleteNonExistedId/tag.yml",
            "datasets/AnswerResourceController/tryToDeleteNonExistedId/user.yml",
            "datasets/AnswerResourceController/tryToDeleteNonExistedId/role.yml",
            "datasets/AnswerResourceController/tryToDeleteNonExistedId/question.yml",
            "datasets/AnswerResourceController/tryToDeleteNonExistedId/questionHasTag.yml",
            "datasets/AnswerResourceController/tryToDeleteNonExistedId/reputation.yml",
            "datasets/AnswerResourceController/tryToDeleteNonExistedId/voteAnswer.yml"
    })
    void tryToDeleteNonExistedId() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.delete("/api/user/question/100/answer/104")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet(value = {
            "datasets/AnswerResourceController/getAnswerByQuestionId/answer.yml",
            "datasets/AnswerResourceController/getAnswerByQuestionId/question.yml",
            "datasets/AnswerResourceController/getAnswerByQuestionId/questionHasTag.yml",
            "datasets/AnswerResourceController/getAnswerByQuestionId/tag.yml",
            "datasets/AnswerResourceController/getAnswerByQuestionId/reputation.yml",
            "datasets/AnswerResourceController/getAnswerByQuestionId/role.yml",
            "datasets/AnswerResourceController/getAnswerByQuestionId/user.yml",
            "datasets/AnswerResourceController/getAnswerByQuestionId/voteAnswer.yml"
    })
    public void getAnswerByQuestionId() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/api/user/question/100/answer")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(100)))
                .andExpect(jsonPath("$[0].userReputation", is(23)))
                .andExpect(jsonPath("$[0].countValuable", is(2)))
                .andExpect(jsonPath("$[1].id", is(101)))
                .andExpect(jsonPath("$[1].userReputation", is(106)))
                .andExpect(jsonPath("$[1].countValuable", is(-2)));
    }

    @Test
    @DataSet(value = {
            "datasets/AnswerResourceController/getEmptyListAnswerByQuestionId/comment.yml",
            "datasets/AnswerResourceController/getEmptyListAnswerByQuestionId/commentAnswer.yml",
            "datasets/AnswerResourceController/getEmptyListAnswerByQuestionId/answer.yml",
            "datasets/AnswerResourceController/getEmptyListAnswerByQuestionId/question.yml",
            "datasets/AnswerResourceController/getEmptyListAnswerByQuestionId/questionHasTag.yml",
            "datasets/AnswerResourceController/getEmptyListAnswerByQuestionId/tag.yml",
            "datasets/AnswerResourceController/getEmptyListAnswerByQuestionId/reputation.yml",
            "datasets/AnswerResourceController/getEmptyListAnswerByQuestionId/role.yml",
            "datasets/AnswerResourceController/getEmptyListAnswerByQuestionId/user.yml",
            "datasets/AnswerResourceController/getEmptyListAnswerByQuestionId/voteAnswer.yml"
    })
    public void getEmptyListAnswerByQuestionId() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.get("/api/user/question/2000/answer")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    @DataSet(value = {
            "datasets/AnswerResourceController/setUpVoteAnswerByAnswerId/answer.yml",
            "datasets/AnswerResourceController/setUpVoteAnswerByAnswerId/question.yml",
            "datasets/AnswerResourceController/setUpVoteAnswerByAnswerId/questionHasTag.yml",
            "datasets/AnswerResourceController/setUpVoteAnswerByAnswerId/tag.yml",
            "datasets/AnswerResourceController/setUpVoteAnswerByAnswerId/reputation.yml",
            "datasets/AnswerResourceController/setUpVoteAnswerByAnswerId/role.yml",
            "datasets/AnswerResourceController/setUpVoteAnswerByAnswerId/user.yml",
            "datasets/AnswerResourceController/setUpVoteAnswerByAnswerId/voteAnswer.yml"
    })
    public void setUpVoteAnswerByAnswerId() throws Exception {
        //Проверяем возвращаемое значение. В датасетах в базе данных уже было 2 голоса ЗА ответ с id 100
        this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/100/upVote")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        //Проверяем, что в БД появилась запись о голосовании от пользователя с id 100 (наш авторизованный юзер) по ответу с id 100
        Assertions.assertTrue(em.createQuery("SELECT v.vote FROM VoteAnswer v WHERE v.user.id = :user AND v.answer.id = :answer")
                .setParameter("user", 100L)
                .setParameter("answer", 100L)
                .getSingleResult()
                .toString()
                .contentEquals("UP_VOTE"));

        //Проверяем, что в БД изменилась репутация пользователя с id 101 (автор) по ответу с id 100. В датасетах изначальная репутация была 106
        Assertions.assertTrue(em.createQuery("SELECT SUM(r.count) FROM Reputation r WHERE r.author.id = :author")
                .setParameter("author", 101L)
                .getSingleResult()
                .toString()
                .contentEquals("116"));

        //Проверяем, что невозможно проголосовать за свой ответ. Ответ с id 103 принадлежит пользователю с id 100("3user@mail.ru", "3111")
        Assertions.assertTrue(this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/103/answer/103/upVote")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .contains("Voting for your answer with id " + 103 + " not allowed"));

        //Проверяем невозможность проголосовать дважды за один ответ, как за, так и против
        Assertions.assertTrue(this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/100/upVote")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .contains("You allready voted for the answer with id " + 100));

        Assertions.assertTrue(this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/100/upVote")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .contains("You allready voted for the answer with id " + 100));

        //Проверяем, что невозможно проголосовать за ответ, которого нет
        Assertions.assertTrue((this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/104/upVote")
                .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .contains("Answer with id " + 104 + " not found")));

    }

    @Test
    @DataSet(value = {
            "datasets/AnswerResourceController/setDownVoteAnswerByAnswerId/answer.yml",
            "datasets/AnswerResourceController/setDownVoteAnswerByAnswerId/question.yml",
            "datasets/AnswerResourceController/setDownVoteAnswerByAnswerId/questionHasTag.yml",
            "datasets/AnswerResourceController/setDownVoteAnswerByAnswerId/tag.yml",
            "datasets/AnswerResourceController/setDownVoteAnswerByAnswerId/reputation.yml",
            "datasets/AnswerResourceController/setDownVoteAnswerByAnswerId/role.yml",
            "datasets/AnswerResourceController/setDownVoteAnswerByAnswerId/user.yml",
            "datasets/AnswerResourceController/setDownVoteAnswerByAnswerId/voteAnswer.yml"
    })

    public void setDownVoteAnswerByAnswerId() throws Exception {
        //Проверяем возвращаемое значение. В датасетах в базе данных уже было 2 голоса ЗА ответ с id 100
        this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/100/downVote")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        //Проверяем, что в БД появилась запись о голосовании от пользователя с id 100 (наш авторизованный юзер) по ответу с id 100
        Assertions.assertTrue(em.createQuery("SELECT v.vote FROM VoteAnswer v WHERE v.user.id = :user AND v.answer.id = :answer")
                .setParameter("user", 100L)
                .setParameter("answer", 100L)
                .getSingleResult()
                .toString()
                .contentEquals("DOWN_VOTE"));

        //Проверяем, что в БД изменилась репутация пользователя с id 101 (автор) по ответу с id 100. В датасетах изначальная репутация была 106
        Assertions.assertTrue(em.createQuery("SELECT SUM(r.count) FROM Reputation r WHERE r.author.id = :author")
                .setParameter("author", 101L)
                .getSingleResult()
                .toString()
                .contentEquals("101"));

        //Проверяем, что невозможно проголосовать за свой ответ. Ответ с id 103 принадлежит пользователю с id 101("3user@mail.ru", "3111")
        Assertions.assertTrue(this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/103/answer/103/downVote")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .contains("Voting for your answer with id " + 103 + " not allowed"));

        //проверяем невозможность проголосовать дважды за один ответ, как за, так и против
        Assertions.assertTrue(this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/100/upVote")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .contains("You allready voted for the answer with id " + 100));

        Assertions.assertTrue(this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/100/downVote")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .contains("You allready voted for the answer with id " + 100));

        //Проверяем, что невозможно проголосовать за ответ, которого нет
        Assertions.assertTrue((this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/104/upVote")
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .contains("Answer with id " + 104 + " not found")));
    }

    @Test
    @DataSet(value = {
            "datasets/AnswerResourceController/addNewAnswer/answer.yml",
            "datasets/AnswerResourceController/addNewAnswer/question.yml",
            "datasets/AnswerResourceController/addNewAnswer/questionHasTag.yml",
            "datasets/AnswerResourceController/addNewAnswer/tag.yml",
            "datasets/AnswerResourceController/addNewAnswer/reputation.yml",
            "datasets/AnswerResourceController/addNewAnswer/role.yml",
            "datasets/AnswerResourceController/addNewAnswer/user.yml",
            "datasets/AnswerResourceController/addNewAnswer/voteAnswer.yml"
    })
    public void addNewAnswer() throws Exception {
        AnswerBodyDto answerBodyDto = new AnswerBodyDto("test");
        AnswerBodyDto answerBodyDtoNull = null;

        //Проверяем возвращаемое значение.
        this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/add")
                        .content(objectMapper.writeValueAsString(answerBodyDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(100L))
                .andExpect(jsonPath("$.image").value("image100"))
                .andExpect(jsonPath("$.nickName").value("user100"))
                .andExpect(jsonPath("$.questionId").value(100L))
                .andExpect(jsonPath("$.userReputation").value(20))
                .andExpect(jsonPath("$.isHelpful").value(false))
                .andExpect(jsonPath("$.countValuable").value(0))
                .andExpect(jsonPath("$.body").value("test"));

        //Проверяем, что в БД появилась запись о новом ответе с id 1
        Assertions.assertTrue(em.createQuery("SELECT a FROM Answer a WHERE a.user.id = :user AND a.id = :answer")
                .setParameter("user", 100L)
                .setParameter("answer", 1L)
                .getResultList().size() > 0);

        //Проверяем, что при повторном добавлении ответа ничего не добавляется в базу
        this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/add")
                        .content(objectMapper.writeValueAsString(answerBodyDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest());

        //Проверяем, что в БД не появилась запись о новом ответе с id 2
        Assertions.assertEquals(0, em.createQuery("SELECT a FROM Answer a WHERE a.user.id = :user AND a.id = :answer")
                .setParameter("user", 100L)
                .setParameter("answer", 2L)
                .getResultList().size());

        //проверяем на несуществующий вопрос
        this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/99/answer/add")
                        .content(objectMapper.writeValueAsString(answerBodyDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest());

        //проверяем на null
        this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/add")
                        .content(objectMapper.writeValueAsString(answerBodyDtoNull))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet(value = {
            "datasets/AnswerResourceController/addNewCommentAnswer/answer.yml",
            "datasets/AnswerResourceController/addNewCommentAnswer/question.yml",
            "datasets/AnswerResourceController/addNewCommentAnswer/questionHasTag.yml",
            "datasets/AnswerResourceController/addNewCommentAnswer/tag.yml",
            "datasets/AnswerResourceController/addNewCommentAnswer/reputation.yml",
            "datasets/AnswerResourceController/addNewCommentAnswer/role.yml",
            "datasets/AnswerResourceController/addNewCommentAnswer/user.yml",
            "datasets/AnswerResourceController/addNewCommentAnswer/voteAnswer.yml",
            "datasets/AnswerResourceController/addNewCommentAnswer/comment.yml",
            "datasets/AnswerResourceController/addNewCommentAnswer/commentAnswer.yml"
    })
    @Transactional
    public void addNewCommentForAnswer() throws Exception {
        AnswerBodyDto answerBodyDto = new AnswerBodyDto("test");
        AnswerBodyDto answerBodyDtoNull = null;

        //Проверяем возвращаемый статус
        this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/100/comment")
                        .content(objectMapper.writeValueAsString(answerBodyDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isOk());

        //проверяем на пустой комментарий
        this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/105/comment")
                        .content(objectMapper.writeValueAsString(answerBodyDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest());

        //Проверяем, что в БД появилась запись с новым комментарием "test"
        User user = em.find(User.class, 100L);
        Answer answer = em.find(Answer.class, 100L);
        CommentAnswer commentAnswer = new CommentAnswer("test", user, answer);
        em.persist(commentAnswer);

        Assertions.assertTrue(em. createQuery("SELECT a FROM CommentAnswer a WHERE a.comment.text = :comment_text")
                .setParameter("comment_text", "test")
                .getResultList().size() > 0);

        //проверяем на несуществующий комментарий
        this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/1/comment")
                        .content(objectMapper.writeValueAsString(answerBodyDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest());

        //проверяем на null
        this.mvc.perform(MockMvcRequestBuilders.post("/api/user/question/100/answer/1/comment")
                        .content(objectMapper.writeValueAsString(answerBodyDtoNull))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getJwtToken("3user@mail.ru", "3111")))
                .andExpect(status().isBadRequest());
    }
}