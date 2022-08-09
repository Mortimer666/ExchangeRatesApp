package com.litviniuk.exchangerates.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;

public interface CurrencyService {
    ResponseEntity<JsonNode> getAllRatesAndSaveThemInDatabase(String date) throws JsonProcessingException;

    ResponseEntity<JsonNode> getRateForSpecificCurrencyOnDate(Integer id, String date) throws JsonProcessingException;
}
