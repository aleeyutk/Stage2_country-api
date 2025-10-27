package com.haidara.countryapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ExternalCountry {
    private String name;
    private String capital;
    private String region;
    private Long population;
    private String flag;
    
    @JsonProperty("currencies")
    private List<Currency> currencies;

    public static class Currency {
        private String code;
        private String name;
        private String symbol;

        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCapital() { return capital; }
    public void setCapital(String capital) { this.capital = capital; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Long getPopulation() { return population; }
    public void setPopulation(Long population) { this.population = population; }
    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }
    public List<Currency> getCurrencies() { return currencies; }
    public void setCurrencies(List<Currency> currencies) { this.currencies = currencies; }
}
