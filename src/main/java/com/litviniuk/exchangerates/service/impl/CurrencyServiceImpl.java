package com.litviniuk.exchangerates.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.litviniuk.exchangerates.dto.MyCurrencyDto;
import com.litviniuk.exchangerates.entity.MyCurrency;
import com.litviniuk.exchangerates.exception.TroublesWithJsonException;
import com.litviniuk.exchangerates.exception.WrongDateException;
import com.litviniuk.exchangerates.repository.CurrencyRepository;
import com.litviniuk.exchangerates.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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

    @Autowired
    public CurrencyServiceImpl(CurrencyRepository currencyRepository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.currencyRepository = currencyRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<String> getAllRatesAndSaveThemInDatabase(String date) {
        String getAllRatesOnDate = "https://www.nbrb.by/api/exrates/rates?ondate=%s&periodicity=0";
        validateStringDateForAllRates(date);
        String currencyResponse = restTemplate.getForObject(String.format(getAllRatesOnDate, date), String.class);
        if (currencyRepository.getMyCurrencyByDateIs(formatDateToRusFormat(date)) != null) {
            throw new WrongDateException(date);
        }
        List<MyCurrency> myCurrencies = mapJsonToListOfCurrencies(date, currencyResponse);
        currencyRepository.saveAll(myCurrencies);
        String responseBody = String.format("Курсы валют установленные %s успешно сохранены", formatDateToRusFormat(date));
        return new ResponseEntity<>(responseBody, getHttpHeaderCRC32(responseBody), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getRateForSpecificCurrencyOnDate(Integer id, String date) {
        if (id < 1 || id > 514) {
            throw new IllegalArgumentException(
                    String.format("Id в пределах от 1 до 514 включительно. Вы ввели = %d", id));
        }
        validateStringDateForRateTakenById(date);
        String getRateForSpecificCurrencyOnDate = "https://www.nbrb.by/api/exrates/rates/%d?ondate=%s";
        String currencyResponse;
        try {
            currencyResponse = restTemplate.getForObject(String.format(
                    getRateForSpecificCurrencyOnDate, id, date), String.class);
        } catch (HttpClientErrorException e) {
            throw new HttpClientErrorException(
                    e.getStatusCode(), "ошибка. Данные, которые Вы запросили, не были найдены.");
        }
        JsonNode currencyNode = mapJsonToJsonNode(currencyResponse);
        BigDecimal rateOnCurrentDate = new BigDecimal(currencyNode.findPath(CUR_OFFICIAL_RATE).asText());
        String currencyResponseOnPreviousBusinessDay;
        StringBuilder partOfResponse;
        try {
            currencyResponseOnPreviousBusinessDay = restTemplate.getForObject(String.format(
                    getRateForSpecificCurrencyOnDate, id, getPreviousBusinessDay(date)), String.class);
            JsonNode currencyNodeOnPreviousBusinessDay = mapJsonToJsonNode(currencyResponseOnPreviousBusinessDay);
            BigDecimal rateOnPreviousDate = new BigDecimal(
                    currencyNodeOnPreviousBusinessDay.findPath(CUR_OFFICIAL_RATE).asText());
            partOfResponse = new StringBuilder("По сравнению с предыдущим рабочим днем курс ");
            if (rateOnCurrentDate.compareTo(rateOnPreviousDate) < 0) {
                partOfResponse.append("снизился");
            } else if (rateOnCurrentDate.compareTo(rateOnPreviousDate) > 0) {
                partOfResponse.append("поднялся");
            } else {
                partOfResponse.append("не изменился");
            }
        } catch (HttpClientErrorException e) {
            partOfResponse = new StringBuilder();
        }
        String responseBody = buildMyCurrencyDto(formatDateToRusFormat(date), currencyNode) + ". " + partOfResponse;
        return new ResponseEntity<>(responseBody, getHttpHeaderCRC32(responseBody), HttpStatus.OK);
    }

    private HttpHeaders getHttpHeaderCRC32(String responseBody) {
        CRC32 crc = new CRC32();
        crc.update(responseBody.getBytes());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("CRC32", String.valueOf(crc.getValue()));
        return responseHeaders;
    }

    private String getPreviousBusinessDay(String date) {
        LocalDate localDate = convertStringToLocalDate(date);
        switch (localDate.get(ChronoField.DAY_OF_WEEK)) {
            case 1:
            case 7:
                return localDate.minusDays(3).toString();
            case 2:
            case 3:
            case 4:
            case 5:
                return localDate.minusDays(1).toString();
            case 6:
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

    private MyCurrencyDto buildMyCurrencyDto(String date, JsonNode currencyNode) {
        MyCurrencyDto currencyDto = new MyCurrencyDto();
        currencyDto.setCurrencyId(Integer.decode(currencyNode.findPath(CUR_ID).asText()));
        currencyDto.setName(currencyNode.findPath(CUR_NAME).asText());
        currencyDto.setAbbreviation(currencyNode.findPath(CUR_ABBREVIATION).asText());
        currencyDto.setDate(date);
        currencyDto.setRate(new BigDecimal(currencyNode.findPath(CUR_OFFICIAL_RATE).asText()));
        currencyDto.setScale(Integer.decode(currencyNode.findPath(CUR_SCALE).asText()));
        return currencyDto;
    }

    private JsonNode mapJsonToJsonNode(String currencyResponse) {
        JsonNode parent;
        try {
            parent = objectMapper.readTree(currencyResponse);
        } catch (JsonProcessingException e) {
            throw new TroublesWithJsonException("Не удалось получить корректные данные от стороннего API");
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
            throw new DateTimeException("Веддите дату в формате: гггг-м-д");
        }
        return localDate;
    }

    private void validateStringDateForAllRates(String date) {
        LocalDate localDate = convertStringToLocalDate(date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("ru"));
        if (localDate.isBefore(LocalDate.of(1995, 3, 29))
                || localDate.isAfter(LocalDate.now())) {
            throw new WrongDateException(String.format(
                    "Неверная дата: %s. Корректные даты в диапазоне от 29 марта 1995 до %s",
                    date, LocalDate.now().format(formatter)));
        }
    }

    private void validateStringDateForRateTakenById(String date) {
        LocalDate localDate = convertStringToLocalDate(date);
        if (localDate.isBefore(LocalDate.of(1991, 1, 1))
                || localDate.isAfter(LocalDate.now())) {
            throw new WrongDateException(String.format("Неверная дата: %s.", date));
        }
    }
}
