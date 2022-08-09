package com.litviniuk.exchangerates.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.litviniuk.exchangerates.dto.MyCurrencyDto;
import com.litviniuk.exchangerates.entity.MyCurrency;
import com.litviniuk.exchangerates.repository.CurrencyRepository;
import com.litviniuk.exchangerates.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.zip.CRC32;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String CUR_ID = "Cur_ID";
    private static final String CUR_NAME = "Cur_Name";
    private static final String CUR_ABBREVIATION = "Cur_Abbreviation";
    private static final String CUR_OFFICIAL_RATE = "Cur_OfficialRate";
    private static final String CUR_SCALE = "Cur_Scale";
    @Value("${api.all.rates.url}")
    private String getAllRatesOnDate;
    @Value("${api.single.rate.url}")
    String getRateForSpecificCurrencyOnDate;

    @Autowired
    public CurrencyServiceImpl(CurrencyRepository currencyRepository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.currencyRepository = currencyRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<JsonNode> getAllRatesAndSaveThemInDatabase(String date) {
        if (!currencyRepository.getAllByDateIs(formatDateToRusFormat(date)).isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "Rates for the given date: %s are already stored in the database. Try to enter a different date.",
                    date));
        }
        String currencyResponse;
        try {
            currencyResponse = restTemplate.getForObject(String.format(getAllRatesOnDate, date), String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IllegalArgumentException(String.format(
                    "Third-party API crashed with entered date: %s. Http status code: %d", date, e.getRawStatusCode()));
        }
        List<MyCurrency> myCurrencies = mapJsonToListOfCurrencies(date, currencyResponse);
        if (myCurrencies.isEmpty()) {
            throw new IllegalArgumentException(String.format("Third-party API returned empty list on date: %s", date));
        }
        currencyRepository.saveAll(myCurrencies);
        JsonNode responseBody = objectMapper.valueToTree(myCurrencies);
        return new ResponseEntity<>(responseBody, getHttpHeaderCRC32(responseBody), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<JsonNode> getRateForSpecificCurrencyOnDate(Integer id, String date) {
        Optional<MyCurrency> requestedCurrencyOptional = currencyRepository.getMyCurrencyByCurrencyIdAndDate(
                id, formatDateToRusFormat(date));
        if (requestedCurrencyOptional.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "Could not find records in the database for the specified parameters: id:%d, date:%s", id, date));
        }
        MyCurrencyDto myCurrencyDto = convertMyCurrencyToMyCurrencyDto(requestedCurrencyOptional.get());
        Optional<MyCurrency> requestedCurrencyOnPreviousBusinessDayOptional = currencyRepository.
                getMyCurrencyByCurrencyIdAndDate(id, formatDateToRusFormat(getPreviousBusinessDay(date)));
        if (requestedCurrencyOnPreviousBusinessDayOptional.isEmpty()) {
            myCurrencyDto.setExchangeRateDifference("Курс за предыдущий рабочий день не найден");
        } else {
            MyCurrencyDto myCurrencyDtoOnPreviousBusinessDay = convertMyCurrencyToMyCurrencyDto(
                    requestedCurrencyOnPreviousBusinessDayOptional.get());
            comparesRatesOfCurrenciesAndSetExchangeDifference(myCurrencyDto, myCurrencyDtoOnPreviousBusinessDay.getRate());
        }
        JsonNode responseBody = objectMapper.valueToTree(myCurrencyDto);
        return new ResponseEntity<>(responseBody,
                getHttpHeaderCRC32(responseBody), HttpStatus.OK);
    }

    private void comparesRatesOfCurrenciesAndSetExchangeDifference(
            MyCurrencyDto myCurrencyDto, BigDecimal currencyRateOnPreviousBusinessDay) {
        if (myCurrencyDto.getRate().compareTo(currencyRateOnPreviousBusinessDay) < 0) {
            myCurrencyDto.setExchangeRateDifference("Курс снизился");
        } else if (myCurrencyDto.getRate().compareTo(currencyRateOnPreviousBusinessDay) > 0) {
            myCurrencyDto.setExchangeRateDifference("Курс поднялся");
        } else {
            myCurrencyDto.setExchangeRateDifference("Курс не изменился");
        }
    }

    private HttpHeaders getHttpHeaderCRC32(JsonNode responseBody) {
        CRC32 crc = new CRC32();
        crc.update(responseBody.toString().getBytes(StandardCharsets.UTF_8));
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("CRC32", String.valueOf(crc.getValue()));
        return responseHeaders;
    }

    private String getPreviousBusinessDay(String date) {
        LocalDate localDate = convertStringToLocalDate(date);
        switch (localDate.get(ChronoField.DAY_OF_WEEK)) {
            case 1:
                return localDate.minusDays(3).toString();
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return localDate.minusDays(1).toString();
            case 7:
                return localDate.minusDays(2).toString();
            default:
                return date;
        }
    }

    private MyCurrency buildMyCurrency(String date, JsonNode currencyNode) {
        MyCurrency currency = new MyCurrency();
        currency.setCurrencyId(Integer.decode(currencyNode.findPath(CUR_ID).asText()));
        currency.setName(currencyNode.findPath(CUR_NAME).asText());
        currency.setAbbreviation(currencyNode.findPath(CUR_ABBREVIATION).asText());
        currency.setDate(date);
        currency.setRate(new BigDecimal(currencyNode.findPath(CUR_OFFICIAL_RATE).asText()));
        currency.setScale(Integer.decode(currencyNode.findPath(CUR_SCALE).asText()));
        return currency;
    }

    private MyCurrencyDto convertMyCurrencyToMyCurrencyDto(MyCurrency myCurrency) {
        MyCurrencyDto currencyDto = new MyCurrencyDto();
        currencyDto.setCurrencyId(myCurrency.getCurrencyId());
        currencyDto.setName(myCurrency.getName());
        currencyDto.setAbbreviation(myCurrency.getAbbreviation());
        currencyDto.setDate(myCurrency.getDate());
        currencyDto.setRate(myCurrency.getRate());
        currencyDto.setScale(myCurrency.getScale());
        return currencyDto;
    }

    private JsonNode mapJsonToJsonNode(String currencyResponse) {
        JsonNode parent;
        try {
            parent = objectMapper.readTree(currencyResponse);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    String.format("Incorrect data from third-party API. %s", e.getMessage()));
        }
        return parent;
    }

    private List<MyCurrency> mapJsonToListOfCurrencies(String date, String currencyResponse) {
        JsonNode parent = mapJsonToJsonNode(currencyResponse);
        Iterator<JsonNode> listOfChildren = parent.elements();
        List<MyCurrency> myCurrencies = new ArrayList<>();
        while (listOfChildren.hasNext()) {
            JsonNode currencyNode = listOfChildren.next();
            myCurrencies.add(buildMyCurrency(formatDateToRusFormat(date), currencyNode));
        }
        return myCurrencies;
    }

    private String formatDateToRusFormat(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("ru"));
        LocalDate localDate = convertStringToLocalDate(date);
        return localDate.format(formatter);
    }

    private LocalDate convertStringToLocalDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date, formatter);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Format your date like: yyyy-M-d");
        }
        return localDate;
    }
}
