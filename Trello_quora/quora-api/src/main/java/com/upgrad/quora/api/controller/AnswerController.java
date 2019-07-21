package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class AnswerController {

    @Autowired
    private AnswerService answerService;

    @Autowired
    private QuestionService questionService;

    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(@PathVariable("questionId") final String questionId,
                                                       @RequestHeader("authorization") final String authorization,
                                                       final AnswerRequest answerRequest)
            throws AuthorizationFailedException, InvalidQuestionException {

        QuestionEntity question = questionService.getQuestion(questionId);
        UserAuthTokenEntity userAuthToken = answerService.authorize(authorization);

        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setUuid(UUID.randomUUID().toString());
        answerEntity.setAns(answerRequest.getAnswer());
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setQuestion(question);
        answerEntity.setUser(userAuthToken.getUser());
        answerEntity = answerService.postAnswer(answerEntity);

        AnswerResponse answerResponse = new AnswerResponse().id(answerEntity.getUuid()).status("ANSWER CREATED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> editAnswerContent(@PathVariable("answerId") final String answerId,
                                                                @RequestHeader("authorization") final String authorization,
                                                                final AnswerEditRequest answerEditRequest)
            throws AuthorizationFailedException, InvalidQuestionException {

        String content = answerEditRequest.getContent();
        AnswerEntity answerEntity = answerService.editAnswerContent(authorization, answerId, content);
        final AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(answerEntity.getUuid()).status("ANSWER EDITED");
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> deleteAnswer(@PathVariable("answerId") final String answerId,
                                                           @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {
        AnswerEntity answerEntity = answerService.deleteAnswer(authorization, answerId);
        final AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(answerEntity.getUuid()).status("ANSWER DELETED");
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/answer/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ResponseEntity<AnswerDetailsResponse>> getAnswersToQuestions(@PathVariable("questionId") final String questionId,
                                                                    @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {
        QuestionEntity question = questionService.validate(questionId);
        List<AnswerEntity> answers = answerService.getAnswersToQuestion(authorization,questionId);

        List<ResponseEntity<AnswerDetailsResponse>> answerDetailsResponses = new ArrayList<>();
        for ( AnswerEntity answerEntity : answers) {
            AnswerDetailsResponse answerDetailsResponse = new AnswerDetailsResponse().id(answerEntity.getUuid()).questionContent(question.getContent()).answerContent(answerEntity.getAns());
            answerDetailsResponses.add(new ResponseEntity<AnswerDetailsResponse>(answerDetailsResponse, HttpStatus.OK));
        }
        return  answerDetailsResponses;
    }

}


