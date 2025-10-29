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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CountryService {

    private static final Logger logger = LoggerFactory.getLogger(CountryService.class);

    private final CountryRepository countryRepository;
    private final RestTemplate restTemplate;
    private final ImageService imageService;

    @Value("${app.external.countries-api:https://restcountries.com/v3.1/all}")
    private String countriesApiUrl;

    @Value("${app.external.exchange-api:https://api.exchangerate-api.com/v4/latest/USD}")
    private String exchangeApiUrl;

    public CountryService(CountryRepository countryRepository, RestTemplate restTemplate, ImageService imageService) {
        this.countryRepository = countryRepository;
        this.restTemplate = restTemplate;
        this.imageService = imageService;
    }

    /** Fetch data from APIs and save countries */
    public void refreshCountries() {
        try {
            logger.info("Starting countries refresh...");

            // Fetch countries data
            ResponseEntity<ExternalCountry[]> countriesResponse =
                    restTemplate.getForEntity(countriesApiUrl, ExternalCountry[].class);

            if (!countriesResponse.getStatusCode().is2xxSuccessful() || countriesResponse.getBody() == null)
                throw new RuntimeException("Failed to fetch countries data");

            // Fetch exchange rates
            ResponseEntity<ExchangeRateResponse> exchangeResponse =
                    restTemplate.getForEntity(exchangeApiUrl, ExchangeRateResponse.class);

            Map<String, Double> exchangeRates = new HashMap<>();
            if (exchangeResponse.getStatusCode().is2xxSuccessful() && exchangeResponse.getBody() != null)
                exchangeRates = exchangeResponse.getBody().getRates();

            List<Country> countriesToSave = new ArrayList<>();

            for (ExternalCountry ext : countriesResponse.getBody()) {
                String name = ext.getName();
                if (name == null || name.isBlank()) continue;

                String currencyCode = null;
                if (ext.getCurrencies() != null && !ext.getCurrencies().isEmpty()) {
                    currencyCode = ext.getCurrencies().keySet().iterator().next();
                }

                Double rate = (currencyCode != null) ? exchangeRates.get(currencyCode) : null;
                if (rate == null || rate == 0) continue;

                Double gdp = calculateEstimatedGdp(ext.getPopulation(), rate);

                Country c = new Country(
                        name.split(",")[0], // normalize
                        ext.getCapital(),
                        ext.getRegion(),
                        ext.getPopulation(),
                        currencyCode,
                        rate,
                        gdp,
                        ext.getFlag()
                );
                c.setLastRefreshedAt(LocalDateTime.now());
                countriesToSave.add(c);
            }

            countryRepository.deleteAll();
            countryRepository.saveAll(countriesToSave);
            imageService.generateSummaryImage();

            logger.info("Saved {} countries", countriesToSave.size());
        } catch (Exception e) {
            logger.error("Failed to refresh countries: {}", e.getMessage());
            throw new RuntimeException("External data source unavailable: " + e.getMessage());
        }
    }

    private Double calculateEstimatedGdp(Long population, Double exchangeRate) {
        if (population == null || exchangeRate == null || exchangeRate == 0) return 0.0;
        double random = ThreadLocalRandom.current().nextDouble(1000, 2001);
        return (population * random) / exchangeRate;
    }

    public List<Country> getAllCountries(String region, String currency, String sort) {
        List<Country> countries;
        if (region != null) countries = countryRepository.findByRegionIgnoreCase(region);
        else if (currency != null) countries = countryRepository.findByCurrencyCodeIgnoreCase(currency);
        else countries = countryRepository.findAll();

        if (sort != null && !countries.isEmpty()) {
            switch (sort.toLowerCase()) {
                case "gdp_desc" -> countries.sort((a, b) -> Double.compare(b.getEstimatedGdp(), a.getEstimatedGdp()));
                case "gdp_asc" -> countries.sort((a, b) -> Double.compare(a.getEstimatedGdp(), b.getEstimatedGdp()));
                case "population_desc" -> countries.sort((a, b) -> Long.compare(b.getPopulation(), a.getPopulation()));
                case "population_asc" -> countries.sort((a, b) -> Long.compare(a.getPopulation(), b.getPopulation()));
            }
        }
        return countries;
    }

    public Optional<Country> getCountryByName(String name) {
        return countryRepository.findByNameIgnoreCase(name);
    }

    public void deleteCountryByName(String name) {
        countryRepository.findByNameIgnoreCase(name)
                .ifPresentOrElse(countryRepository::delete, () -> {
                    throw new RuntimeException("Country not found");
                });
    }

    public Map<String, Object> getStatus() {
        long total = countryRepository.count();
        Optional<LocalDateTime> last = countryRepository.findLastRefreshTime();

        Map<String, Object> status = new HashMap<>();
        status.put("total_countries", total);
        status.put("last_refreshed_at", last
                .map(t -> t.atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT))
                .orElse(null));
        return status;
    }
}
