package com.haidara.countryapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Country {

    @Id
    private String name;
    private String capital;
    private String region;
    private Long population;

    @JsonProperty("currency_code")
    private String currencyCode;

    @JsonProperty("exchange_rate")
    private Double exchangeRate;

    @JsonProperty("estimated_gdp")
    private Double estimatedGdp;

    private String flagUrl;
    private LocalDateTime lastRefreshedAt;

    public Country() {}

    public Country(String name, String capital, String region, Long population,
                   String currencyCode, Double exchangeRate,
                   Double estimatedGdp, String flagUrl) {
        this.name = name;
        this.capital = capital;
        this.region = region;
        this.population = population;
        this.currencyCode = currencyCode;
        this.exchangeRate = exchangeRate;
        this.estimatedGdp = estimatedGdp;
        this.flagUrl = flagUrl;
        this.lastRefreshedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCapital() { return capital; }
    public void setCapital(String capital) { this.capital = capital; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Long getPopulation() { return population; }
    public void setPopulation(Long population) { this.population = population; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public Double getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(Double exchangeRate) { this.exchangeRate = exchangeRate; }

    public Double getEstimatedGdp() { return estimatedGdp; }
    public void setEstimatedGdp(Double estimatedGdp) { this.estimatedGdp = estimatedGdp; }

    public String getFlagUrl() { return flagUrl; }
    public void setFlagUrl(String flagUrl) { this.flagUrl = flagUrl; }

    public LocalDateTime getLastRefreshedAt() { return lastRefreshedAt; }
    public void setLastRefreshedAt(LocalDateTime lastRefreshedAt) { this.lastRefreshedAt = lastRefreshedAt; }
}
