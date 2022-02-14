package com.javamentor.qa.platform.dao.impl.dto.pagination;

import com.javamentor.qa.platform.dao.abstracts.dto.pagination.PaginationDtoAble;
import com.javamentor.qa.platform.models.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;


@Repository("AllUserDtoSortVote")
@RequiredArgsConstructor
public class GetAllUserSortVote implements PaginationDtoAble <UserDto> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<UserDto> getItems(Map<String, Object> param) {
        int currentPageNumber = (int) param.get("currentPageNumber");
        int itemsOnPage = (int) param.get("itemsOnPage");

        return entityManager.createQuery ("SELECT new com.javamentor.qa.platform.models.dto.UserDto" +
                        "(e.id, e.email, e.fullName, e.imageLink, e.city, " +
                        "sum(r.count), e.persistDateTime) " +
                        "FROM User e left outer JOIN Reputation r ON (e.id=r.author.id) " +
                        "LEFT JOIN VoteAnswer va ON (e.id = va.user.id) " +
                        "LEFT JOIN VoteQuestion vq ON (e.id = vq.user.id)" +
                        "group by e.id " +
                        "ORDER BY SUM (SELECT COUNT(*) FROM VoteQuestion vq WHERE vq.vote = 'UP_VOTE') -" +
                        "(SELECT COUNT(*) FROM VoteQuestion vq WHERE vq.vote = 'DOWN_VOTE') +" +
                        "(SELECT COUNT (*) FROM VoteAnswer va WHERE va.vote = 'UP_VOTE') -" +
                        "(SELECT COUNT (*) FROM VoteAnswer va WHERE va.vote = 'DOWN_VOTE')", UserDto.class)
                                .setFirstResult((currentPageNumber - 1) * itemsOnPage)
                                .setMaxResults(itemsOnPage)
                                .getResultList();
    }

    @Override
    public int getTotalResultCount(Map<String, Object> param) {
        return ((Long) entityManager.createQuery("SELECT COUNT (u) FROM  User u").getSingleResult()).intValue();
    }
}