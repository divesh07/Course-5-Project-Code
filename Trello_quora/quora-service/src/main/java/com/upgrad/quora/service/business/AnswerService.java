package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserAuthTokenDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.DeleteFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnswerService {

    @Autowired
    private UserAuthTokenDao userAuthTokenDao;

    @Autowired
    private AnswerDao answerDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity authorize(final String authorization) throws AuthorizationFailedException {

        UserAuthTokenEntity userAuthTokenEntity = userAuthTokenDao.getUserAuthToken(authorization);

        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not Signed in.");
        }

        // Check the user log out time , if the value is not null then the user has signed out
        if ( userAuthTokenEntity.getLoginAt() !=null && userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post an answer");
        }
        return userAuthTokenEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity postAnswer(final AnswerEntity answerEntity)  {
        return answerDao.postAnswer(answerEntity);
    }

}
