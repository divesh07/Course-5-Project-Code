package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class AnswerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public AnswerEntity postAnswer(AnswerEntity answerEntity) {
        entityManager.persist(answerEntity);
        return answerEntity;
    }

    public AnswerEntity editAnswer(final AnswerEntity answerEntity){
        return entityManager.merge(answerEntity);
    }

    public AnswerEntity getAnswer(final String uuid){
        try {
            return entityManager.createNamedQuery("answerById", AnswerEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

}
