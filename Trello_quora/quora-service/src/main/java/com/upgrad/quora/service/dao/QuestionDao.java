package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class QuestionDao {
    @PersistenceContext
    private EntityManager entityManager;

    public QuestionEntity createQuestion(final QuestionEntity questionEntity){
        entityManager.persist(questionEntity);
        return questionEntity;
    }

    public QuestionEntity getAllQuestions(final UserEntity userEntity){
        try {
            return entityManager.createNamedQuery("questionByUserId", QuestionEntity.class).setParameter("user", userEntity).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

}
