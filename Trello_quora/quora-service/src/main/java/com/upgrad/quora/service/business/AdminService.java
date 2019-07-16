package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.DeleteFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    @Autowired
    private UserDao userDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity userDelete(final String userUuid, final String authorization) throws AuthorizationFailedException, UserNotFoundException, DeleteFailedException {

        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorization);

        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not Signed in.");
        }

        // Check the user log out time , if the value is not null then the user has signed out
        if ( userAuthTokenEntity.getLoginAt() !=null && userAuthTokenEntity.getLogoutAt() != null){
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.");
        }

        String role = userAuthTokenEntity.getUser().getRole();
        if ( role.equalsIgnoreCase("nonadmin")){
            throw new AuthorizationFailedException("ATHR-003", "Unauthorized Access, Entered user is not an admin");
        }

        UserEntity userEntity = userDao.getUserById(userUuid);
        if ( null == userEntity){
            throw new UserNotFoundException("USR-001","User with entered uuid to be deleted does not exist");
        }

        userEntity = userDao.deleteUserById(userUuid);
        if ( null == userEntity){
            throw new DeleteFailedException("DEL-001","Failed to delete user");
        }

        return userEntity;
    }
}
