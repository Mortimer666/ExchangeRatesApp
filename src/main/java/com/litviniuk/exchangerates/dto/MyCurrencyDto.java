package com.litviniuk.exchangerates.dto;

import java.math.BigDecimal;

public class MyCurrencyDto {

    private Integer currencyId;
    private String name;
    private String abbreviation;
    private String date;
    private BigDecimal rate;
    private Integer scale;

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

    @Override
    public String toString() {
        return String.format("Валюта - %d %s(%s, внутренний код - %d). Курс на дату - %s составляет %s",
                scale, name, abbreviation, currencyId, date, rate.toString());
    }
}
