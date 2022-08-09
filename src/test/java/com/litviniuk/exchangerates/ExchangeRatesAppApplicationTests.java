package com.litviniuk.exchangerates;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExchangeRatesAppApplicationTests {

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldThrow() {
//        String test = "{\"name\":\"value\"}";
//        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
//        Mockito.when(restTemplate.getForObject("1", String.class)).thenReturn(("f"));
//        Assertions.assertThrows(TroublesWithJsonException.class, () -> objectMapper.readTree(test));
    }

}
