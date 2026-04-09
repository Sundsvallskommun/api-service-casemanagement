# CaseManagement

_A service that acts as an integration layer between clients and multiple case management systems, providing unified
access to:_

- _Byggr (Building permits)_
- _Ecos (Environmental and health protection)_
- _Alk-T (Alcohol permits)_
- _CaseData (Citizen-related cases)_

## Getting Started

### Prerequisites

- **Java 25 or higher**
- **Maven**
- **MariaDB**
- **Git**
- **[Dependent Microservices](#dependencies)**

### Installation

1. **Clone the repository:**

   ```bash
   git clone git@github.com:Sundsvallskommun/api-service-casemanagement.git
   cd api-service-casemanagement
   ```
2. **Configure the application:**

   Before running the application, you need to set up configuration settings.
   See [Configuration](#Configuration)

   **Note:** Ensure all required configurations are set; otherwise, the application may fail to start.

3. **Ensure dependent services are running:**

   If this microservice depends on other services, make sure they are up and accessible.
   See [Dependencies](#dependencies) for more details.

4. **Build and run the application:**

   ```bash
   mvn spring-boot:run
   ```

## Dependencies

This microservice depends on the following services:

- **Alk-T**
  - **Purpose:** A service for retrieving owners, their establishments and ongoing cases regarding alcohol permits.
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-alkt)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **CaseData**
  - **Purpose:** Manages cases primarily related to citizen-related subjects.
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-case-data)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Lantmäteriet Registerbeteckning**
  - **Purpose:** Get register reference to a property.
  - **Repository:** Not available at this moment.
  - **Additional Notes:** This is a API serving data
    from [Lantmäteriet](https://www.lantmateriet.se/sv/geodata/vara-produkter/produktlista/registerbeteckning-direkt/).
- **Messaging**
  - **Purpose:** Used to send messages if something goes wrong with cases.
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-messaging)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Party**
  - **Purpose:**  To translate between partyId and legalId for a stakeholder, person or organization.
  - **Repository:** [Link to the repository](https://github.com/Sundsvallskommun/api-service-party)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Sokigo ByggR(Arendeexport)**
  - **Purpose:** Manages cases related to building permits
  - **External URL:** [Sokigo ByggR](https://sokigo.com/produkter/byggr/)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Sokigo Ecos(Minutmiljo)**
  - **Purpose:** Manages cases related to environmental and health protection supervision
  - **External URL:** [Sokigo Ecos](https://sokigo.com/produkter/ecos/)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Sokigo FB**
  - **Purpose:** Provides basic data on property and population information.
  - **External URL:** [Sokigo FB](https://sokigo.com/produkter/fastighet-och-befolkning/)
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **Citizen**
  - **Purpose:** Provides citizen mapping between party IDs and legal IDs.
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **EventLog**
  - **Purpose:** Logs events related to case processing.
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **OeP Integrator**
  - **Purpose:** To report back status to OpenE platform.
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.
- **EDP Future**
  - **Purpose:** Integration with the EDP Future case management system.
  - **Setup Instructions:** Refer to its documentation for installation and configuration steps.

Ensure that these services are running and properly configured before starting this microservice.

## API Documentation

Access the API documentation via Swagger UI:

- **Swagger UI:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

Alternatively, refer to the `openapi.yml` file located in `src/main/resources/api` for the OpenAPI specification.

## Usage

### API Endpoints

Refer to the [API Documentation](#api-documentation) for detailed information on available endpoints.

### Example Request

```bash
curl -X GET http://localhost:8080/2281/cases/case-mappings
```

## Configuration

Configuration is crucial for the application to run successfully. Ensure all necessary settings are configured in
`application.yml`.

### Key Configuration Parameters

- **Server Port:**

  ```yaml
  server:
    port: 8080
  ```
- **Database Settings:**

  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/your_database
      username: your_db_username
      password: your_db_password
  ```
- **External Service Configuration:**

  ```yaml
  integration:
    arendeexport:
      url: http://your_arendeexport_url
    case-data:
      oauth2ClientId: some_client_id
      oauth2ClientSecret: some_token_secret
      oauth2TokenUrl: https://your_token_url
      url: http://your_casedata_url
      namespaces:
        2281:
          - MY_NAMESPACE
          - MY_OTHER_NAMESPACE
    alk-t:
      oauth2ClientId: some_client_id
      oauth2ClientSecret: some_token_secret
      oauth2TokenUrl: https://your_token_url
      url: http://your_alkt_url
    citizen:
      oauth2ClientId: some_client_id
      oauth2ClientSecret: some_token_secret
      oauth2TokenUrl: https://your_token_url
      url: http://your_citizen_url
    eventlog:
      oauth2ClientId: some_client_id
      oauth2ClientSecret: some_token_secret
      oauth2TokenUrl: https://your_token_url
      url: http://your_eventlog_url
    fb:
      database: your_db_name
      url: http://your_fb_url
      username: your_fb_username
      password: your_fb_password
    lantmateriet:
      oauth2ClientId: your_lantmateriet_client_id
      oauth2ClientSecret: your_lantmateriet_client_secret
      oauth2TokenUrl: https://your_token_url
      registerbeteckning:
        url: http://your_lantmateriet_registerbeteckning_url
    messaging:
      channel: channel-to-post-in
      client-id: some_client_id
      client-secret: some_token_secret
      token-uri: https://your_token_url
      url: https://your_messaging_url
      token: your_messaging_token
    party:
      oauth2ClientId: some_client_id
      oauth2ClientSecret: some_token_secret
      oauth2TokenUrl: https://your_token_url
      url: http://your_party_url
    minutmiljo:
      url: https://your_MinutMiljoService.svc_url
      username: your_minutmiljo_username
      password: your_minutmiljo_password
    oep-integrator:
      oauth2ClientId: some_client_id
      oauth2ClientSecret: some_token_secret
      oauth2TokenUrl: https://your_token_url
      url: http://your_oep_integrator_url
    edp-future:
      url: http://your_edp_future_url
      api-key: your_api_key
  ```

### Database Initialization

The project is set up with [Flyway](https://github.com/flyway/flyway) for database migrations. Flyway is enabled by
default and will automatically populate the database schema upon application startup.

- **No additional setup is required** for database initialization, as long as the database connection settings are
  correctly configured.

### Additional Notes

- **Application Profiles:**

  Use Spring profiles (`dev`, `prod`, etc.) to manage different configurations for different environments.

- **Logging Configuration:**

  Adjust logging levels if necessary.

## Case Types

Case types are database-driven and determine which backend system handles a given case. The routing is configured in the `case_type` table.

### Adding Case Types

The approach depends on which system the case type belongs to:

#### CaseData

No database migration is needed. Case types that are not found in the `case_type` table automatically default to CaseData. The types are validated dynamically against the CaseData API based on the configured namespaces per municipality in `application.yml`:

```yaml
integration:
  case-data:
    namespaces:
      2281:
        - MY_NAMESPACE
```

#### Byggr

Create a Flyway migration that inserts into both `case_type` and `byggr_case_type_config`:

```sql
INSERT INTO case_type (name, system_type, nullable_facility_type, nullable_facility, facility_type_rule)
VALUES ('MY_NEW_TYPE', 'BYGGR', 0, 0, NULL);

INSERT INTO byggr_case_type_config (case_type_name, arende_slag, arende_grupp, arende_typ, handelse_typ, handelse_rubrik, handelse_slag, arende_mening)
VALUES ('MY_NEW_TYPE', 'SLAG', 'GRP', 'TYP', 'HTYP', 'Rubrik', 'HSLAG', 'Mening');
```

#### Ecos

Create a Flyway migration that inserts into both `case_type` and `ecos_case_type_config`:

```sql
INSERT INTO case_type (name, system_type, nullable_facility_type, nullable_facility)
VALUES ('MY_NEW_TYPE', 'ECOS', 0, 0);

INSERT INTO ecos_case_type_config (case_type_name, diary_plan_id, process_type_id, facility_handler)
VALUES ('MY_NEW_TYPE', 'diary-uuid', 'process-uuid', 'FOOD');
```

Available facility handlers: `FOOD`, `HEAT_PUMP`, `INDIVIDUAL_SEWAGE`, `HEALTH_PROTECTION`, `RISK_CLASS_UPDATE`, `EXISTING_FACILITY`, `NONE`.

## Contributing

Contributions are welcome! Please
see [CONTRIBUTING.md](https://github.com/Sundsvallskommun/.github/blob/main/.github/CONTRIBUTING.md) for guidelines.

## License

This project is licensed under the [MIT License](LICENSE).

## Code status

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-casemanagement&metric=alert_status)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-casemanagement)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-casemanagement&metric=reliability_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-casemanagement)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-casemanagement&metric=security_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-casemanagement)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-casemanagement&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-casemanagement)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-casemanagement&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-casemanagement)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sundsvallskommun_api-service-casemanagement&metric=bugs)](https://sonarcloud.io/summary/overall?id=Sundsvallskommun_api-service-casemanagement)

---

© 2026 Sundsvalls kommun
