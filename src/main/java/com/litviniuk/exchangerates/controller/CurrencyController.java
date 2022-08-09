package com.litviniuk.exchangerates.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.litviniuk.exchangerates.service.CurrencyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/exchange-rates-app")
public class CurrencyController {

    private static final Logger LOGGER = LogManager.getLogger(CurrencyController.class);
    private final CurrencyService currencyService;

    @Autowired
    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/get-all-rates-on-date/{date}")
    public ResponseEntity<JsonNode> getAllRatesAndSaveThemInDatabase(
            @PathVariable String date,
            HttpServletRequest request) throws JsonProcessingException {
        LOGGER.info("Request - Method: {}. Protocol/version: {}. Full path: {}:{}{}",
                request.getMethod(),
                request.getProtocol(),
                request.getServerName(),
                request.getServerPort(),
                request.getRequestURI());
        ResponseEntity<JsonNode> resp = currencyService.getAllRatesAndSaveThemInDatabase(date);
        LOGGER.info("Response - {}", resp);
        return resp;
    }

    @GetMapping("/get-rate-for-specific-currency-on-date/{id}/{date}")
    public ResponseEntity<JsonNode> getRateForSpecificCurrencyOnDate(
            @PathVariable Integer id,
            @PathVariable String date,
            HttpServletRequest request) throws JsonProcessingException {
        LOGGER.info("Request - Method: {}. Protocol/version: {}. Full path: {}:{}{}",
                request.getMethod(),
                request.getProtocol(),
                request.getServerName(),
                request.getServerPort(),
                request.getRequestURI());
        ResponseEntity<JsonNode> resp = currencyService.getRateForSpecificCurrencyOnDate(id, date);
        LOGGER.info("Response - {}", resp);
        return resp;
    }
}
