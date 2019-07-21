package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserAuthTokenDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
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

    @Autowired
    private UserAuthTokenDao userAuthTokenDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity signout(final String authorization) throws SignOutRestrictedException {

        UserAuthTokenEntity userAuthTokenEntity = userAuthTokenDao.getUserAuthToken(authorization);
        if (userAuthTokenEntity == null) {
            throw new SignOutRestrictedException("SGR-001", "User is not Signed in.");
        }
        userAuthTokenEntity.setLogoutAt(ZonedDateTime.now());
        userAuthTokenDao.updateAuthToken(userAuthTokenEntity);

        return userAuthTokenEntity;
    }
}


