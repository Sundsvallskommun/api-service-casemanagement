# CaseManagement

## Leverantör
Sundsvalls kommun

## Beskrivning
CaseManagement är en tjänst som hanterar ärenden mot ByggR, Ecos2 och vårt egna ärendehanteringssystem CaseData.

## Tekniska detaljer
### Integrationer
Tjänsten integrerar mot:

* Lantmäteriet
* Sokigo FB
* ArendeExport (Byggr)
* MinutMiljo (Ecos2)
* Citizen
* CaseData

### Starta tjänsten

| Miljövariabel                       | Beskrivning                              |
|-------------------------------------|------------------------------------------|
| **Databasinställningar**            ||
| `DB_URL`                            | JDBC-URL för anslutning till databas     |
| `DB_USER`                           | Användarnamn för anslutning till databas |
| `DB_PASS`                           | Lösenord för anslutning till databas     |
| **Integration Sundsvalls kommun**   ||
| `SUNDSVALLS_KOMMUN_CONSUMER_KEY`    | Oauth2 key                               |
| `SUNDSVALLS_KOMMUN_CONSUMER_SECRET` | Oauth2 secret                            |
| `SUNDSVALLS_KOMMUN_INTERNAL_ORIGIN` | URL                                      |
| `SUNDSVALLS_KOMMUN_TOKEN_PATH`      | Sökväg för att hämta token               |
| **Integration Lantmäteriet**        ||
| `LANTMATERIET_CONSUMER_KEY`         | Oauth2 key                               |
| `LANTMATERIET_CONSUMER_SECRET`      | Oauth2 secret                            |
| `LANTMATERIET_ORIGIN`               | URL                                      |
| `LANTMATERIET_TOKEN_PATH`           | Sökväg för att hämta token               |
| **Integration Sokigo FB**           ||
| `FB_DB`                             | Databas i FB                             |
| `FB_USER`                           | Användarnamn                             |
| `FB_PASS`                           | Lösenord                                 |
| `FB_ORIGIN`                         | URL                                      |
| **Integration ArendeExport**        ||
| `ARENDEEXPORT_ORIGIN`               | URL                                      |
| **Integration MinutMiljo**          ||
| `MINUT_MILJO_ORIGIN`                | URL                                      |
| `MINUTMILJO_USER`                   | Användarnamn                             |
| `MINUTMILJO_PASS`                   | Lösenord                                 |


### Paketera och starta tjänsten
Applikationen kan köras lokalt med detta kommando:

```
./mvnw spring-boot:run -Dspring.profiles.active=local
```

## Status
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-casemanagement&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-casemanagement)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-casemanagement&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-casemanagement)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-casemanagement&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-casemanagement)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-casemanagement&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-casemanagement)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-casemanagement&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-casemanagement)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-casemanagement&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-casemanagement)


## 
Copyright (c) 2021 Sundsvalls kommun
