package com.haidara.countryapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "countries")
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(unique = true, nullable = false)
    private String name;

    private String capital;
    private String region;

    @NotNull(message = "Population is required")
    private Long population;

    @Column(name = "currency_code")
    private String currencyCode;

    @Column(name = "exchange_rate")
    private Double exchangeRate;

    @Column(name = "estimated_gdp")
    private Double estimatedGdp;

    @Column(name = "flag_url")
    private String flagUrl;

    @Column(name = "last_refreshed_at")
    private LocalDateTime lastRefreshedAt;

    // Constructors
    public Country() {}

    public Country(String name, String capital, String region, Long population, 
                   String currencyCode, Double exchangeRate, Double estimatedGdp, 
                   String flagUrl) {
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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
