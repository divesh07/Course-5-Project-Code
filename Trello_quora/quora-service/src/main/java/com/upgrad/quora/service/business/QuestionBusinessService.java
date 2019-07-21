package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;


@Service
public class QuestionBusinessService {

    @Autowired
    private CommonBusinessService commonBusinessService;

    @Autowired
    private QuestionDao questionDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(final QuestionEntity questionEntity, final String authorization) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity=commonBusinessService.getUser(authorization);
        if(userAuthTokenEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }else {
            ZonedDateTime expiryTime=userAuthTokenEntity.getExpiresAt();
            ZonedDateTime logoutTime=userAuthTokenEntity.getLogoutAt();
            ZonedDateTime nowTime=ZonedDateTime.now();
            if(nowTime.compareTo(expiryTime)>0)
                throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post a question");
            if(logoutTime!=null){
                if(nowTime.compareTo(logoutTime)>0)
                    throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post a question");
            }
            questionEntity.setUser(userAuthTokenEntity.getUser());
            return questionDao.createQuestion(questionEntity);
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<QuestionEntity> getAllQuestions(final String authorization) throws AuthorizationFailedException{
        UserAuthTokenEntity userAuthTokenEntity=commonBusinessService.getUser(authorization);
        if(userAuthTokenEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }else {
            ZonedDateTime expiryTime=userAuthTokenEntity.getExpiresAt();
            ZonedDateTime logoutTime=userAuthTokenEntity.getLogoutAt();
            ZonedDateTime nowTime=ZonedDateTime.now();
            if(nowTime.compareTo(expiryTime)>0)
                throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions");
            if(logoutTime!=null){
                if(nowTime.compareTo(logoutTime)>0)
                    throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions");
            }
            return questionDao.getAllQuestions();
        }

    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<QuestionEntity> getAllQuestionsByUser(final String authorization,final String userId) throws AuthorizationFailedException,UserNotFoundException{
        UserAuthTokenEntity userAuthTokenEntity=commonBusinessService.getUser(authorization);
        if(userAuthTokenEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }else {
            ZonedDateTime expiryTime=userAuthTokenEntity.getExpiresAt();
            ZonedDateTime logoutTime=userAuthTokenEntity.getLogoutAt();
            ZonedDateTime nowTime=ZonedDateTime.now();
            if(nowTime.compareTo(expiryTime)>0)
                throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions posted by a specific user");
            if(logoutTime!=null){
                if(nowTime.compareTo(logoutTime)>0)
                    throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions posted by a specific users");
            }
            UserEntity userEntity=userAuthTokenEntity.getUser();
            List<QuestionEntity> questionEntities=questionDao.getAllQuestionsByUser(userAuthTokenEntity.getUser());
            if(questionEntities!=null) {
                if (userEntity.getUuid().equals(userId))
                    return questionEntities;
                else
                    throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");
            }else
                throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity editQuestionContent(final String authorization,final String questionId,final String content) throws AuthorizationFailedException,InvalidQuestionException{
        QuestionEntity questionEntity=getQuestion(authorization,questionId,"edit");
        questionEntity.setContent(content);
        return questionDao.editQuestion(questionEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestion(final String authorization,final String questionId) throws AuthorizationFailedException,InvalidQuestionException{
        QuestionEntity questionEntity=getQuestion(authorization,questionId,"delete");
        return questionDao.deleteQuestion(questionEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity getQuestion(final String authorization,final String questionId,String type) throws AuthorizationFailedException,InvalidQuestionException{
        UserAuthTokenEntity userAuthTokenEntity=commonBusinessService.getUser(authorization);
        if(userAuthTokenEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }else {
            ZonedDateTime expiryTime=userAuthTokenEntity.getExpiresAt();
            ZonedDateTime logoutTime=userAuthTokenEntity.getLogoutAt();
            ZonedDateTime nowTime=ZonedDateTime.now();
            if(nowTime.compareTo(expiryTime)>0)
                throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to "+type+" the question");
            if(logoutTime!=null){
                if(nowTime.compareTo(logoutTime)>0)
                    throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to "+type+" the question");
            }
            QuestionEntity questionEntity=questionDao.getQuestion(questionId);
            if(questionEntity!=null) {
                UserEntity signedUser = userAuthTokenEntity.getUser();
                UserEntity owner = questionEntity.getUser();
                if (!owner.getUuid().equals(signedUser.getUuid())) {
                    if(type.equals("delete"))
                        throw new AuthorizationFailedException("ATHR-003","Only the question owner or admin can delete the question");
                    else
                        throw new AuthorizationFailedException("ATHR-003","Only the question owner can "+type+" the question");
                }
                return questionEntity;
            }else
                throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }

    }

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
        if ( question == null){
            throw new InvalidQuestionException("QUES-001",errorMessage);
        }
        return question;
    }

}
