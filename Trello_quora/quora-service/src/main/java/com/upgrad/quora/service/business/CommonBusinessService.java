package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserAuthTokenDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommonBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAuthTokenDao userAuthTokenDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity getUser(final String userUuid, final String authorization) throws AuthorizationFailedException, UserNotFoundException {

        UserAuthTokenEntity userAuthTokenEntity = userAuthTokenDao.getUserAuthToken(authorization);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not Signed in.");
        }

        // Check the user log out time , if the value is not null then the user has signed out
        if (userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details.");
        }

        UserEntity userEntity = userDao.getUserById(userUuid);

        if ( null == userEntity){
            throw new UserNotFoundException("USR-001","User with entered uuid does not exist");
        }

        return userEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity getUser(final String authorization){
        UserAuthTokenEntity userAuthTokenEntity=userAuthTokenDao.getUserAuthToken(authorization);
        return userAuthTokenEntity;
    }

}


