package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class QuestionDao {
    @PersistenceContext
    private EntityManager entityManager;

    public QuestionEntity createQuestion(final QuestionEntity questionEntity) {
        entityManager.persist(questionEntity);
        return questionEntity;
    }

    public List<QuestionEntity> getAllQuestions(){
        return entityManager.createNamedQuery("questionsAll", QuestionEntity.class).getResultList();
    }

    public List<QuestionEntity> getAllQuestionsByUser(final UserEntity userEntity){
        try {
            return entityManager.createNamedQuery("questionsByUserId", QuestionEntity.class).setParameter("user",userEntity).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public QuestionEntity getQuestion(final String uuid) {
        try {
            return entityManager.createNamedQuery("questionByUuid", QuestionEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public QuestionEntity editQuestion(final QuestionEntity questionEntity) {
        return entityManager.merge(questionEntity);
    }

    public QuestionEntity deleteQuestion(final QuestionEntity questionEntity) {
        entityManager.remove(questionEntity);
        return questionEntity;
    }

}
