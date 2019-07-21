package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.AdminService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.DeleteFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Admin controller to be used by admin to delete user provided its ID ( uudid)
     *
     * @param userUuid
     * @param authorization
     * @return
     * @throws UserNotFoundException
     * @throws AuthorizationFailedException
     * @throws DeleteFailedException
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/admin/user/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDeleteResponse> deleteUser(@PathVariable("userId") final String userUuid, @RequestHeader("authorization") final String authorization) throws UserNotFoundException, AuthorizationFailedException, DeleteFailedException {

        final UserEntity userEntity = adminService.userDelete(userUuid, authorization);

        UserDeleteResponse userDeleteResponse = new UserDeleteResponse().id(userEntity.getUuid())
                .status("USER SUCCESSFULLY DELETED ");

        return new ResponseEntity<UserDeleteResponse>(userDeleteResponse, HttpStatus.OK);
    }
}
