package com.litviniuk.exchangerates.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;

public interface CurrencyService {
    ResponseEntity<String> getAllRatesAndSaveThemInDatabase(String date) throws JsonProcessingException;

    ResponseEntity<String> getRateForSpecificCurrencyOnDate(Integer id, String date) throws JsonProcessingException;
}
