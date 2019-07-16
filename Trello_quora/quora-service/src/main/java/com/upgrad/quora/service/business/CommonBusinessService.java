package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class CommonBusinessService {

    @Autowired
    private UserDao userDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity getUser(final String authorization) throws AuthorizationFailedException, UserNotFoundException {

        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorization);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not Signed in.");
        }

        UserEntity userEntity = userAuthTokenEntity.getUser();
        // Check the user log out time , if the value is not null then the user has signed out
        if ( userEntity.getLastLoginAt() !=null && userEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details.");
        }

        String userId = userEntity.getUuid();
        userEntity = userDao.getUserById(userId);

        if ( null == userEntity){
            throw new UserNotFoundException("USR-001","User with entered uuid does not exist");
        }

        return userEntity;
    }
}


