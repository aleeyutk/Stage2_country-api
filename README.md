# Country Currency & Exchange API

A Spring Boot REST API that fetches country data from external APIs, calculates GDP estimates, and provides CRUD operations with caching.

## 🚀 Features

- **RESTful API** with JSON responses
- **External API Integration** - REST Countries & Exchange Rates
- **GDP Calculation** - Estimated GDP based on population and exchange rates
- **Caching** - Database caching with refresh endpoint
- **Filtering & Sorting** - By region, currency, GDP, population
- **Image Generation** - Summary image with top countries
- **Health Checks** - API status monitoring

## 📋 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/countries/refresh` | Fetch and cache countries data |
| `GET` | `/api/countries` | Get all countries (supports filters) |
| `GET` | `/api/countries/{name}` | Get country by name |
| `DELETE` | `/api/countries/{name}` | Delete country by name |
| `GET` | `/api/status` | Get API status and counts |
| `GET` | `/api/countries/image` | Get summary image |

### Query Parameters for `/api/countries`

- `region` - Filter by region (e.g., `Africa`, `Europe`)
- `currency` - Filter by currency code (e.g., `NGN`, `USD`)
- `sort` - Sort order: `gdp_desc`, `gdp_asc`, `population_desc`, `population_asc`

## 🛠️ Tech Stack

- **Java 17** - Programming language
- **Spring Boot 3.1.0** - Application framework
- **H2 Database** - Embedded database (development)
- **Maven** - Dependency management
- **Docker** - Containerization

## 📦 Installation & Local Development

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- (Optional) Docker

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd country-currency-api
Build the application

bash
mvn clean package
Run the application

bash
mvn spring-boot:run
Test the API

bash
# Check status
curl http://localhost:8080/api/status

# Refresh data
curl -X POST http://localhost:8080/api/countries/refresh

# Get countries
curl http://localhost:8080/api/countries
Environment Variables
Create a .env file or set environment variables:

bash
# Server
SERVER_PORT=8080

# Database (H2)
SPRING_DATASOURCE_URL=jdbc:h2:file:./data/countrydb
SPRING_DATASOURCE_USERNAME=sa
SPRING_DATASOURCE_PASSWORD=password

# External APIs
COUNTRIES_API_URL=https://restcountries.com/v2/all?fields=name,capital,region,population,flag,currencies
EXCHANGE_API_URL=https://open.er-api.com/v6/latest/USD
REQUEST_TIMEOUT=10000

# Logging
LOGGING_LEVEL_COM_HAIDARA=INFO
📊 API Examples
Refresh Countries Data
bash
curl -X POST http://localhost:8080/api/countries/refresh
Response:

json
{
  "message": "Countries refreshed successfully"
}
Get Countries with Filters
bash
curl "http://localhost:8080/api/countries?region=Africa&sort=gdp_desc"
Response:

json
[
  {
    "id": 1,
    "name": "Nigeria",
    "capital": "Abuja",
    "region": "Africa",
    "population": 206139589,
    "currency_code": "NGN",
    "exchange_rate": 1600.23,
    "estimated_gdp": 25767448125.2,
    "flag_url": "https://flagcdn.com/ng.svg",
    "last_refreshed_at": "2025-10-25T22:30:00Z"
  }
]
Get API Status
bash
curl http://localhost:8080/api/status
Response:

json
{
  "total_countries": 250,
  "last_refreshed_at": "2025-10-25T22:30:00Z"
}
🧪 Testing
Manual Testing
bash
# Run the comprehensive test script
chmod +x test-api.sh
./test-api.sh
Automated Tests
bash
# Run unit tests
mvn test

# Run with test coverage
mvn jacoco:report
📁 Project Structure
text
country-currency-api/
├── src/main/java/com/haidara/countryapi/
│   ├── CountryApiApplication.java      # Main application
│   ├── controller/CountryController.java
│   ├── service/{CountryService.java, ImageService.java}
│   ├── repository/CountryRepository.java
│   ├── model/{Country.java, ExternalCountry.java, ExchangeRateResponse.java}
│   └── config/RestTemplateConfig.java
├── src/main/resources/
│   └── application.yml                 # Configuration
├── data/                              # Database files (H2)
├── cache/                             # Generated images
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
🔧 Configuration
Database Options
H2 (Default - Development):

yaml
SPRING_DATASOURCE_URL=jdbc:h2:file:./data/countrydb
PostgreSQL (Production):

yaml
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/countrydb
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
External APIs
Countries API: https://restcountries.com/v2/all

Exchange Rates: https://open.er-api.com/v6/latest/USD

🐛 Troubleshooting
Common Issues
Port already in use

bash
# Change port in .env
SERVER_PORT=8081
External API failures

Check internet connection

Verify API endpoints are accessible

Increase timeout in configuration

Database issues

Delete data/ directory and restart

Check database file permissions

Logs
bash
# View application logs
tail -f logs/application.log

# Docker logs
docker logs country-api
📄 License
This project is licensed under the MIT License.

🤝 Contributing
Fork the repository

Create a feature branch

Commit your changes

Push to the branch

Create a Pull Request
