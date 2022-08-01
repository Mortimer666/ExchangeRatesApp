package com.litviniuk.exchangerates.repository;

import com.litviniuk.exchangerates.entity.MyCurrency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<MyCurrency, Integer> {
    MyCurrency getMyCurrencyByDateIs(String date);
}
