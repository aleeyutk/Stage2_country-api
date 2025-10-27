package com.haidara.countryapi.service;

import com.haidara.countryapi.model.Country;
import com.haidara.countryapi.model.ExternalCountry;
import com.haidara.countryapi.model.ExchangeRateResponse;
import com.haidara.countryapi.repository.CountryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CountryService {
    
    private static final Logger logger = LoggerFactory.getLogger(CountryService.class);
    
    private final CountryRepository countryRepository;
    private final RestTemplate restTemplate;
    private final ImageService imageService;
    
    @Value("${app.external.countries-api}")
    private String countriesApiUrl;
    
    @Value("${app.external.exchange-api}")
    private String exchangeApiUrl;
    
    public CountryService(CountryRepository countryRepository, RestTemplate restTemplate, ImageService imageService) {
        this.countryRepository = countryRepository;
        this.restTemplate = restTemplate;
        this.imageService = imageService;
    }
    
    public void refreshCountries() {
        try {
            logger.info("Starting countries refresh...");
            
            // Fetch countries data
            ResponseEntity<ExternalCountry[]> countriesResponse = 
                restTemplate.getForEntity(countriesApiUrl, ExternalCountry[].class);
            
            if (!countriesResponse.getStatusCode().is2xxSuccessful() || countriesResponse.getBody() == null) {
                throw new RuntimeException("Failed to fetch countries data");
            }
            
            // Fetch exchange rates
            ResponseEntity<ExchangeRateResponse> exchangeResponse = 
                restTemplate.getForEntity(exchangeApiUrl, ExchangeRateResponse.class);
            
            Map<String, Double> exchangeRates = new HashMap<>();
            if (exchangeResponse.getStatusCode().is2xxSuccessful() && exchangeResponse.getBody() != null) {
                exchangeRates = exchangeResponse.getBody().getRates();
            }
            
            // Process countries
            List<Country> countriesToSave = new ArrayList<>();
            for (ExternalCountry externalCountry : countriesResponse.getBody()) {
                Country country = processCountry(externalCountry, exchangeRates);
                if (country != null) {
                    countriesToSave.add(country);
                }
            }
            
            // Save all countries
            countryRepository.saveAll(countriesToSave);
            
            // Generate summary image
            imageService.generateSummaryImage();
            
            logger.info("Successfully refreshed {} countries", countriesToSave.size());
            
        } catch (Exception e) {
            logger.error("Failed to refresh countries: {}", e.getMessage());
            throw new RuntimeException("External data source unavailable: " + e.getMessage());
        }
    }
    
    private Country processCountry(ExternalCountry externalCountry, Map<String, Double> exchangeRates) {
        try {
            // Get currency code (first currency or null)
            String currencyCode = null;
            if (externalCountry.getCurrencies() != null && !externalCountry.getCurrencies().isEmpty()) {
                currencyCode = externalCountry.getCurrencies().get(0).getCode();
            }
            
            // Get exchange rate
            Double exchangeRate = null;
            if (currencyCode != null && exchangeRates.containsKey(currencyCode)) {
                exchangeRate = exchangeRates.get(currencyCode);
            }
            
            // Calculate estimated GDP
            Double estimatedGdp = calculateEstimatedGdp(
                externalCountry.getPopulation(), 
                exchangeRate
            );
            
            // Check if country exists
            Optional<Country> existingCountry = countryRepository.findByNameIgnoreCase(externalCountry.getName());
            Country country;
            
            if (existingCountry.isPresent()) {
                country = existingCountry.get();
                // Update existing country
                country.setCapital(externalCountry.getCapital());
                country.setRegion(externalCountry.getRegion());
                country.setPopulation(externalCountry.getPopulation());
                country.setCurrencyCode(currencyCode);
                country.setExchangeRate(exchangeRate);
                country.setEstimatedGdp(estimatedGdp);
                country.setFlagUrl(externalCountry.getFlag());
                country.setLastRefreshedAt(LocalDateTime.now());
            } else {
                // Create new country
                country = new Country(
                    externalCountry.getName(),
                    externalCountry.getCapital(),
                    externalCountry.getRegion(),
                    externalCountry.getPopulation(),
                    currencyCode,
                    exchangeRate,
                    estimatedGdp,
                    externalCountry.getFlag()
                );
            }
            
            return country;
            
        } catch (Exception e) {
            logger.warn("Failed to process country {}: {}", externalCountry.getName(), e.getMessage());
            return null;
        }
    }
    
    private Double calculateEstimatedGdp(Long population, Double exchangeRate) {
        if (population == null || exchangeRate == null || exchangeRate == 0) {
            return 0.0;
        }
        
        // Generate random multiplier between 1000 and 2000
        double randomMultiplier = ThreadLocalRandom.current().nextDouble(1000, 2001);
        
        // Calculate GDP: population × random(1000–2000) ÷ exchange_rate
        return (population * randomMultiplier) / exchangeRate;
    }
    
    public List<Country> getAllCountries(String region, String currency, String sort) {
        List<Country> countries;
        
        if (region != null) {
            countries = countryRepository.findByRegionIgnoreCase(region);
        } else if (currency != null) {
            countries = countryRepository.findByCurrencyCodeIgnoreCase(currency);
        } else {
            countries = countryRepository.findAll();
        }
        
        // Apply sorting
        if (sort != null) {
            switch (sort.toLowerCase()) {
                case "gdp_desc":
                    countries.sort((c1, c2) -> Double.compare(c2.getEstimatedGdp(), c1.getEstimatedGdp()));
                    break;
                case "gdp_asc":
                    countries.sort((c1, c2) -> Double.compare(c1.getEstimatedGdp(), c2.getEstimatedGdp()));
                    break;
                case "population_desc":
                    countries.sort((c1, c2) -> Long.compare(c2.getPopulation(), c1.getPopulation()));
                    break;
                case "population_asc":
                    countries.sort((c1, c2) -> Long.compare(c1.getPopulation(), c2.getPopulation()));
                    break;
            }
        }
        
        return countries;
    }
    
    public Optional<Country> getCountryByName(String name) {
        return countryRepository.findByNameIgnoreCase(name);
    }
    
    public void deleteCountryByName(String name) {
        Optional<Country> country = countryRepository.findByNameIgnoreCase(name);
        if (country.isPresent()) {
            countryRepository.delete(country.get());
        } else {
            throw new RuntimeException("Country not found");
        }
    }
    
    public Map<String, Object> getStatus() {
        long totalCountries = countryRepository.count();
        Optional<LocalDateTime> lastRefresh = countryRepository.findLastRefreshTime();
        
        Map<String, Object> status = new HashMap<>();
        status.put("total_countries", totalCountries);
        status.put("last_refreshed_at", lastRefresh.orElse(null));
        
        return status;
    }
}
