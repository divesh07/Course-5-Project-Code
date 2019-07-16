package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class SignOutBusinessService {

    @Autowired
    private UserDao userDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity signout(final String authorization) throws SignOutRestrictedException {

        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorization);
        if (userAuthTokenEntity == null) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in.");
        }

        UserEntity userEntity = userAuthTokenEntity.getUser();
        userEntity.setLogoutAt(ZonedDateTime.now());
        // Added this to clear the jwt token so that the user should not call any other API after logout
        //userAuthTokenEntity.setAccessToken("");
        userDao.updateUser(userEntity);

        // A new check is added which checks if the user log out is set , if true then user wont be able to call other API
        // And hence removing this - clearing of user jwt
        //userDao.updateAuthToken(userAuthTokenEntity);
        return userAuthTokenEntity;
    }
}


