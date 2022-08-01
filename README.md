# Exchange Rates App - Сервис просмотра курсов валют

### Инструкция по запуску:
1. Создать папку в которой вы хотите поместить проект
2. Используя командную строку перейти в ранее созданную папку (команда cd в командной строке Windows)
3. Далее последовательно выполнить указанные команды:
```
git init
```
```
git pull https://github.com/Mortimer666/ExchangeRatesApp/
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
Возвращает курс валют по указанному коду валюты и на указанную дату. Также возвращает данные изменения данного курса по сравнению с предыдущим рабочим днем (снизился/вырос/не изменился).
****
### Описание работы:
Либо наполнить базу данных курсами валют на указанную дату вызвав endpoint (/exchange-rates-app/get-all-rates-on-date/{date}) и указав дату в формате yyyy-M-d.

Либо получить данные о курсе указанной валюты на указанную дату вызвав endpoint(/exchange-rates-app/get-rate-for-specific-currency-on-date/{id}/{date}) и указав id в переделах от 1 до 514 и дату в формате yyyy-M-d.

Не все запросы возвращают данные, так как для определенных id на определенные даты API НБ РБ возвращает 404ую ошибку. В этом случае приложение перехватывает ошибку и возвращает соответствующий ответ.
