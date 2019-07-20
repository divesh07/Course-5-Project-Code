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

import java.time.ZonedDateTime;

@Service
public class AnswerService {

    @Autowired
    private UserAuthTokenDao userAuthTokenDao;

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private CommonBusinessService commonBusinessService;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity authorize(final String authorization) throws AuthorizationFailedException {

        UserAuthTokenEntity userAuthTokenEntity = userAuthTokenDao.getUserAuthToken(authorization);

        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not Signed in.");
        }

        // Check the user log out time , if the value is not null then the user has signed out
        if ( userAuthTokenEntity.getLoginAt() != null && userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to post an answer");
        }
        return userAuthTokenEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity postAnswer(final AnswerEntity answerEntity)  {
        return answerDao.postAnswer(answerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswerContent(final String authorization,final String answerId,final String answer) throws AuthorizationFailedException,InvalidQuestionException{
        AnswerEntity answerEntity = getAnswer( authorization, answerId, "edit");
        answerEntity.setAns(answer);
        return answerDao.editAnswer(answerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(final String authorization,final String answerId) throws AuthorizationFailedException, InvalidQuestionException{
        AnswerEntity answerEntity = getAnswer( authorization, answerId,"delete");
        return answerDao.deleteAnswer(answerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity getAnswer(final String authorization,final String answerId, String type) throws AuthorizationFailedException, InvalidQuestionException{
        UserAuthTokenEntity userAuthTokenEntity = commonBusinessService.getUser(authorization);
        if(userAuthTokenEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }else {
            ZonedDateTime expiryTime=userAuthTokenEntity.getExpiresAt();
            ZonedDateTime logoutTime=userAuthTokenEntity.getLogoutAt();
            ZonedDateTime nowTime=ZonedDateTime.now();
            if(nowTime.compareTo(expiryTime)>0)
                throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to edit an answer");
            if(logoutTime!=null){
                if(nowTime.compareTo(logoutTime)>0)
                    throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to edit an answer");
            }
            AnswerEntity answerEntity = answerDao.getAnswer(answerId);
            if(answerEntity!=null) {
                UserEntity signedUser = userAuthTokenEntity.getUser();
                UserEntity owner = answerEntity.getUser();
                if (!owner.getUuid().equals(signedUser.getUuid()) && signedUser.getRole() != "admin") {
                    if(type.equals("delete"))
                        throw new AuthorizationFailedException("ATHR-003","Only the answer owner or admin can delete the answer");
                    else
                        throw new AuthorizationFailedException("ATHR-003","Only the answer owner can edit the answer");
                }
                return answerEntity;
            }else
                throw new InvalidQuestionException("ANS-001","Entered answer uuid does not exist");
        }

    }

}
