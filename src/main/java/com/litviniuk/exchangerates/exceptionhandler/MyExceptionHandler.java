package com.litviniuk.exchangerates.exceptionhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.CRC32;

@ControllerAdvice
public class MyExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger MYLOGGER = LogManager.getLogger(MyExceptionHandler.class);
    private final ObjectMapper objectMapper;

    @Autowired
    public MyExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest webRequest) {
        Map<String, String> errorResponse = new LinkedHashMap<>();
        errorResponse.put("status", String.valueOf(HttpStatus.BAD_REQUEST));
        errorResponse.put("message", String.format("Incorrect data. Try to change parameters. %s", ex.getMessage()));
        errorResponse.put("date", LocalDate.now().toString());
        CRC32 crc = new CRC32();
        JsonNode jsonNode = objectMapper.valueToTree(errorResponse);
        crc.update(jsonNode.toString().getBytes(StandardCharsets.UTF_8));
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("CRC32", String.valueOf(crc.getValue()));
        ResponseEntity<Object> response = handleExceptionInternal(
                ex, errorResponse, responseHeaders, HttpStatus.BAD_REQUEST, webRequest);
        MYLOGGER.warn("Response with exception - {}", response);
        return response;
    }
}
