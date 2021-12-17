package com.javamentor.qa.platform.dao.impl.model;

import com.javamentor.qa.platform.dao.abstracts.model.UserDao;
import com.javamentor.qa.platform.dao.util.SingleResultUtil;
import com.javamentor.qa.platform.models.entity.user.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImpl extends ReadWriteDaoImpl<User, Long> implements UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<User> getById(Long id) {
        String hql = "SELECT u FROM User u inner join fetch u.role as role where u.id = :id";
        TypedQuery<User> query = entityManager.createQuery(hql, User.class).setParameter("id", id);
        return SingleResultUtil.getSingleResultOrNull(query);
    }

    @Override
    public Optional<User> getByEmail(String email) {
        String hql = "SELECT u FROM User u inner join fetch u.role as role where u.email = :email";
        TypedQuery<User> query = entityManager.createQuery(hql, User.class).setParameter("email", email);
        return SingleResultUtil.getSingleResultOrNull(query);
    }

    @Override
    public List<User> getAll() {
        return entityManager.createQuery("SELECT u FROM User u inner join fetch u.role",
                User.class).getResultList();
    }

    @Override
    public List<User> getAllByIds(Iterable<Long> ids) {
        if (ids != null && ids.iterator().hasNext()) {
            return entityManager.createQuery("select u from User u inner join fetch u.role WHERE u.id IN :ids"
                    , User.class).setParameter("ids", ids).getResultList();
        } else {
            return new ArrayList<>();
        }
    }
}
