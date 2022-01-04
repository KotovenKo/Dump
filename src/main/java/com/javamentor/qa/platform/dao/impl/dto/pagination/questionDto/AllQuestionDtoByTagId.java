package com.javamentor.qa.platform.dao.impl.dto.pagination.questionDto;

import com.javamentor.qa.platform.dao.abstracts.dto.pagination.PaginationDtoAble;
import com.javamentor.qa.platform.models.dto.QuestionDto;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository("AllQuestionDtoByTagId")
public class AllQuestionDtoByTagId implements PaginationDtoAble<QuestionDto> {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<QuestionDto> getItems(Map<String, Object> param) {
        Long id = (Long) param.get("tagId");
        int currentPageNumber = (int) param.get("currentPageNumber");
        int itemsOnPage = (int) param.get("itemsOnPage");
        return entityManager.createQuery(
                "SELECT DISTINCT new com.javamentor.qa.platform.models.dto." +
                        "QuestionDto(question.id, question.title, author.id, " +
                        "(SELECT sum (reputation.count) from Reputation reputation where reputation.author.id = author.id), " +
                        "author.fullName, author.imageLink, " +
                        "question.description, 0L, " +
                        "(SELECT count (*) from Answer answer where answer.question.id = question.id), " +
                        "((SELECT count (*) from VoteQuestion voteOnQuestion " +
                        "where voteOnQuestion.vote = 'UP_VOTE' and voteOnQuestion.question.id = question.id) - " +
                        "(SELECT count (*) from VoteQuestion voteOnQuestion " +
                        "where voteOnQuestion.vote = 'DOWN_VOTE' and voteOnQuestion.question.id = question.id)), " +
                        "question.persistDateTime, question.lastUpdateDateTime) " +
                        "from Question question " +
                        "left outer join question.user as author " +
                        "left outer join question.answers as answer " +
                        "left outer join question.tags as tags " +
                        "where tags.id = :id order by question.id", QuestionDto.class)
                .setParameter("id", id)
                .getResultStream()
                .skip((currentPageNumber-1)*itemsOnPage).limit(itemsOnPage).collect(Collectors.toList());
    }

    @Override
    public int getTotalResultCount(Map<String, Object> param) {
        Long id = (Long) param.get("tagId");
        return entityManager.createQuery(
                        "SELECT DISTINCT new com.javamentor.qa.platform.models.dto." +
                                "QuestionDto(question.id, question.title, author.id, " +
                                "(SELECT sum (reputation.count) from Reputation reputation where reputation.author.id = author.id), " +
                                "author.fullName, author.imageLink, " +
                                "question.description, 0L, " +
                                "(SELECT count (*) from Answer answer where answer.question.id = question.id), " +
                                "((SELECT count (*) from VoteQuestion voteOnQuestion " +
                                "where voteOnQuestion.vote = 'UP_VOTE' and voteOnQuestion.question.id = question.id) - " +
                                "(SELECT count (*) from VoteQuestion voteOnQuestion " +
                                "where voteOnQuestion.vote = 'DOWN_VOTE' and voteOnQuestion.question.id = question.id)), " +
                                "question.persistDateTime, question.lastUpdateDateTime) " +
                                "from Question question " +
                                "left outer join question.user as author " +
                                "left outer join question.answers as answer " +
                                "left outer join question.tags as tags " +
                                "where tags.id = :id order by question.id", QuestionDto.class)
                .setParameter("id", id).getResultList().size();
    }
}
