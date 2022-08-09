package com.litviniuk.exchangerates.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Table(name = "Currency")
@Entity
public class MyCurrency implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "CURRENCY_ID")
    private Integer currencyId;
    @Column(name = "NAME")
    private String name;
    @Column(name = "NAME_ABBREVIATION")
    private String abbreviation;
    @Column(name = "DATE")
    private String date;
    @Column(name = "RATE")
    private BigDecimal rate;
    @Column(name = "SCALE")
    private Integer scale;

    public MyCurrency() {
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
}
