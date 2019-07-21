package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AnswerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public AnswerEntity postAnswer(AnswerEntity answerEntity) {
        entityManager.persist(answerEntity);
        return answerEntity;
    }

    public AnswerEntity editAnswer(final AnswerEntity answerEntity) {
        return entityManager.merge(answerEntity);
    }

    public AnswerEntity deleteAnswer(final AnswerEntity answerEntity) {
        entityManager.remove(answerEntity);
        return answerEntity;
    }

    public AnswerEntity getAnswer(final String uuid) {
        try {
            return entityManager.createNamedQuery("answerById", AnswerEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<AnswerEntity> getAllAnswers(final String question) {
        return entityManager.createNamedQuery("answerbyQuestionId", AnswerEntity.class).setParameter("question", question).getResultList();
    }
}
