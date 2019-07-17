package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class QuestionController {

    @Autowired
    private QuestionBusinessService questionBusinessService;

    @RequestMapping(method = RequestMethod.POST,path = "/question/create",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(@RequestHeader("authorization")final String authorization, final QuestionRequest questionRequest) throws AuthorizationFailedException {
        QuestionEntity questionEntity=new QuestionEntity();
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setDate(ZonedDateTime.now());
        questionEntity.setUuid(UUID.randomUUID().toString());
        final QuestionEntity createdQuestion=questionBusinessService.createQuestion(questionEntity,authorization);
        final QuestionResponse questionResponse=new QuestionResponse().id(createdQuestion.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET,path = "/question/all",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDetailsResponse> getAllQuestions(@RequestHeader("authorization")final String authorization) throws AuthorizationFailedException{
        QuestionEntity questionEntity=questionBusinessService.getAllQuestions(authorization);
        final QuestionDetailsResponse questionDetailsResponse=new QuestionDetailsResponse().content(questionEntity.getContent()).id(questionEntity.getUuid());
        return new ResponseEntity<QuestionDetailsResponse>(questionDetailsResponse,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT,path = "/question/edit/{questionId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionContent(@RequestHeader("authorization")final String authorization, @PathVariable("questionId")final String questionId, final QuestionEditRequest questionEditRequest) throws AuthorizationFailedException, InvalidQuestionException {
        String content=questionEditRequest.getContent();
        QuestionEntity questionEntity=questionBusinessService.editQuestionContent(authorization,questionId,content);
        final QuestionEditResponse questionEditResponse=new QuestionEditResponse().id(questionEntity.getUuid()).status("QUESTION EDITED");
        return new ResponseEntity<QuestionEditResponse>(questionEditResponse,HttpStatus.OK);
    }
}
