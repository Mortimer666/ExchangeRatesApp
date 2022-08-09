package com.litviniuk.exchangerates.repository;

import com.litviniuk.exchangerates.entity.MyCurrency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<MyCurrency, Integer> {
    List<MyCurrency> getAllByDateIs(String date);

    Optional<MyCurrency> getMyCurrencyByCurrencyIdAndDate(Integer id, String date);
}
