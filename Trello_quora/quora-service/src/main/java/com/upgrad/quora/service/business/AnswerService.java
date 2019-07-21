package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.UserAuthTokenDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class AnswerService {

    @Autowired
    private UserAuthTokenDao userAuthTokenDao;

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private CommonBusinessService commonBusinessService;

    public UserAuthTokenEntity validateUserSignOut(UserAuthTokenEntity userAuthTokenEntity, final String errorMessage) throws AuthorizationFailedException {
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not Signed in.");
        }
        // Verify if the Auth token is not expired
        if (ZonedDateTime.now().compareTo(userAuthTokenEntity.getExpiresAt()) > 0) {
            throw new AuthorizationFailedException("ATHR-002", errorMessage);
        }
        // Check the user log out time , if the value is not null then the user has signed out
        if (userAuthTokenEntity.getLoginAt() != null && userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", errorMessage);
        }
        return userAuthTokenEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity authorize(final String authorization) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userAuthTokenDao.getUserAuthToken(authorization);
        return validateUserSignOut(userAuthTokenEntity, "User is signed out.Sign in first to post an answer");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<AnswerEntity> getAnswersToQuestion(final String authorization, final QuestionEntity question) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userAuthTokenDao.getUserAuthToken(authorization);
        userAuthTokenEntity = validateUserSignOut(userAuthTokenEntity, "User is signed out.Sign in first to get the answers");
        return answerDao.getAllAnswers(question);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity postAnswer(final AnswerEntity answerEntity) {
        return answerDao.postAnswer(answerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswerContent(final String authorization, final String answerId, final String answer) throws AuthorizationFailedException, InvalidQuestionException {
        AnswerEntity answerEntity = getAnswer(authorization, answerId, "edit");
        answerEntity.setAns(answer);
        return answerDao.editAnswer(answerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(final String authorization, final String answerId) throws AuthorizationFailedException, InvalidQuestionException {
        AnswerEntity answerEntity = getAnswer(authorization, answerId, "delete");
        return answerDao.deleteAnswer(answerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity getAnswer(final String authorization, final String answerId, String type) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthTokenEntity userAuthTokenEntity = commonBusinessService.getUser(authorization);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        } else {
            ZonedDateTime expiryTime = userAuthTokenEntity.getExpiresAt();
            ZonedDateTime logoutTime = userAuthTokenEntity.getLogoutAt();
            ZonedDateTime loginTime = userAuthTokenEntity.getLoginAt();
            ZonedDateTime nowTime = ZonedDateTime.now();
            // Verify if the Auth token is not expired
            if (nowTime.compareTo(expiryTime) > 0)
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit an answer");
            // Indicates that user is logged in and even log out value is set
            if (loginTime != null && logoutTime != null) {
                if (nowTime.compareTo(logoutTime) > 0)
                    throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit an answer");
            }
            AnswerEntity answerEntity = answerDao.getAnswer(answerId);
            if (answerEntity != null) {
                UserEntity signedUser = userAuthTokenEntity.getUser();
                UserEntity owner = answerEntity.getUser();
                if (!owner.getUuid().equals(signedUser.getUuid()) && signedUser.getRole() != "admin") {
                    if (type.equals("delete"))
                        throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
                    else
                        throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
                }
                return answerEntity;
            } else
                throw new InvalidQuestionException("ANS-001", "Entered answer uuid does not exist");
        }
    }
}
