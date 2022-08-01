package com.litviniuk.exchangerates.exceptionhandler;

import com.litviniuk.exchangerates.exception.TroublesWithJsonException;
import com.litviniuk.exchangerates.exception.WrongDateException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.NonUniqueResultException;
import java.time.DateTimeException;

@ControllerAdvice
public class MyExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger MYLOGGER = LogManager.getLogger(MyExceptionHandler.class);

    @ExceptionHandler(value = {NonUniqueResultException.class})
    protected ResponseEntity<Object> handleConflictWithDuplicateData(RuntimeException ex, WebRequest webRequest) {
        String bodyOfResponse = "Курсы на заданную дату уже сохранены в базе данных. Попробуйте ввести другую дату.";
        ResponseEntity<Object> response = handleExceptionInternal
                (ex, bodyOfResponse, new HttpHeaders(), HttpStatus.CONFLICT, webRequest);
        MYLOGGER.warn("Response with exception - {}", response);
        return response;
    }

    @ExceptionHandler(value = {IllegalArgumentException.class, DateTimeException.class,
            WrongDateException.class, HttpClientErrorException.class, TroublesWithJsonException.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest webRequest) {
        String bodyOfResponse = "Введены неверные данные, попробуйте изменить входные параметры. " + ex.getMessage();
        ResponseEntity<Object> response = handleExceptionInternal
                (ex, bodyOfResponse, new HttpHeaders(), HttpStatus.CONFLICT, webRequest);
        MYLOGGER.warn("Response with exception - {}", response);
        return response;
    }

}
