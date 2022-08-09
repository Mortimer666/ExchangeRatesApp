package com.litviniuk.exchangerates.dto;

import java.math.BigDecimal;

public class MyCurrencyDto {

    private Integer currencyId;
    private String name;
    private String abbreviation;
    private String date;
    private BigDecimal rate;
    private Integer scale;
    private String exchangeRateDifference;

    public MyCurrencyDto() {
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getDate() {
        return date;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public Integer getScale() {
        return scale;
    }

    public String getExchangeRateDifference() {
        return exchangeRateDifference;
    }

    public void setExchangeRateDifference(String exchangeRateDifference) {
        this.exchangeRateDifference = exchangeRateDifference;
    }
}
