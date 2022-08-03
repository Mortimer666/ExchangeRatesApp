# Exchange Rates App - Сервис просмотра курсов валют

### Инструкция по запуску:
1. Используя командную строку перейти в папку, в которой хотите разместить проект (команда cd в командной строке Windows)
2. Далее последовательно выполнить указанные команды:
```
git clone https://github.com/Mortimer666/ExchangeRatesApp/
```
```
cd ExchangeRatesApp
```
```
mvn clean package
```
```
cd target
```
```
java -jar ExchangeRatesApp-0.0.1-SNAPSHOT.jar
```
4. Открыть браузер и перейти по адресу: http://localhost:8080 + использовать один из указанных ниже endpoint'ов.
Например: http://localhost:8080/exchange-rates-app/get-all-rates-on-date/2022-8-1
****
### Описание endpoints:
```
/exchange-rates-app/get-all-rates-on-date/{date}
```
Получает данные о курсах валют на указанную дату и сохраняет их в базе данных(H2 database).
```
/exchange-rates-app/get-rate-for-specific-currency-on-date/{id}/{date}
```
Возвращает курс валют по указанному коду валюты и на указанную дату из базы данных. Также возвращает данные изменения данного курса по сравнению с предыдущим рабочим днем (снизился/вырос/не изменился).
****
### Описание работы:
1. Наполнить базу данных курсами валют на указанную дату вызвав endpoint (/exchange-rates-app/get-all-rates-on-date/{date}) и указав дату в формате yyyy-M-d.

2. Получить данные о курсе указанной валюты на указанную дату из базы данных вызвав endpoint(/exchange-rates-app/get-rate-for-specific-currency-on-date/{id}/{date}) и указав id в переделах от 1 до 514 и дату в формате yyyy-M-d.

При использовании второго endpoint'а есть вероятность не получить указанные данные, если до этого эти данные не были помещены в базу данных используя первый endpoint.
