# API Documentation

## Base URL
http://localhost:8080/api

text

## Endpoints

### 1. Refresh Countries Data
**POST** `/countries/refresh`

Fetches latest country data from external APIs and updates the cache.

**Response:**
```json
{
  "message": "Countries refreshed successfully"
}
Error Responses:

503 Service Unavailable - External APIs unavailable

500 Internal Server Error - Server error

2. Get All Countries
GET /countries

Retrieves all countries with optional filtering and sorting.

Query Parameters:

region (optional) - Filter by region

currency (optional) - Filter by currency code

sort (optional) - Sort order: gdp_desc, gdp_asc, population_desc, population_asc

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
3. Get Country by Name
GET /countries/{name}

Retrieves a specific country by name (case-insensitive).

Path Parameters:

name - Country name

Response:

json
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
Error Responses:

404 Not Found - Country not found

4. Delete Country
DELETE /countries/{name}

Deletes a country from the database.

Path Parameters:

name - Country name

Response:

json
{
  "message": "Country deleted successfully"
}
Error Responses:

404 Not Found - Country not found

5. Get API Status
GET /status

Returns API status and statistics.

Response:

json
{
  "total_countries": 250,
  "last_refreshed_at": "2025-10-25T22:30:00Z"
}
6. Get Summary Image
GET /countries/image

Returns a generated PNG image with country statistics.

Response:

200 OK - PNG image

404 Not Found - Image not available

Data Models
Country
json
{
  "id": "number",
  "name": "string (required)",
  "capital": "string",
  "region": "string",
  "population": "number (required)",
  "currency_code": "string",
  "exchange_rate": "number",
  "estimated_gdp": "number",
  "flag_url": "string",
  "last_refreshed_at": "string (ISO 8601)"
}
Error Response
json
{
  "error": "string",
  "details": "string (optional)"
}
Examples
Filter African Countries by GDP
bash
curl "http://localhost:8080/api/countries?region=Africa&sort=gdp_desc"
Get Countries Using USD
bash
curl "http://localhost:8080/api/countries?currency=USD"
Complete Workflow
bash
# 1. Refresh data
curl -X POST http://localhost:8080/api/countries/refresh

# 2. Check status
curl http://localhost:8080/api/status

# 3. Get top 5 countries by GDP
curl "http://localhost:8080/api/countries?sort=gdp_desc" | jq '.[0:5]'

# 4. Get specific country
curl http://localhost:8080/api/countries/Nigeria

# 5. Get summary image
curl http://localhost:8080/api/countries/image -o summary.png
