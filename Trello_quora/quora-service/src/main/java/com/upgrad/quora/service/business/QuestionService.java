package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

    @Autowired
    private QuestionDao questionDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity getQuestion(final String questionUuid) throws InvalidQuestionException {
        final QuestionEntity question = validateQuestion(questionUuid, "The question entered is invalid");
        return question;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity validate(final String questionUuid) throws InvalidQuestionException {
        final QuestionEntity question = validateQuestion(questionUuid, "The question with entered uuid whose details are to be seen does not exist");
        return question;
    }

    public QuestionEntity validateQuestion(final String questionUuid, final String errorMessage) throws InvalidQuestionException {
        QuestionEntity question = questionDao.getQuestion(questionUuid);
        if (question == null) {
            throw new InvalidQuestionException("QUES-001", errorMessage);
        }
        return question;
    }
}
