package com.haidara.countryapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalCountry {

    @JsonProperty("name")
    private Map<String, Object> nameObj;

    @JsonProperty("capital")
    private List<String> capitalList;

    private String region;
    private Long population;

    @JsonProperty("flags")
    private Map<String, Object> flags;

    private Map<String, Object> currencies;

    // === Derived getters ===

    public String getName() {
        if (nameObj != null && nameObj.get("common") != null)
            return nameObj.get("common").toString();
        return null;
    }

    public String getCapital() {
        return (capitalList != null && !capitalList.isEmpty()) ? capitalList.get(0) : null;
    }

    public String getRegion() { return region; }

    public Long getPopulation() { return population; }

    public String getFlag() {
        if (flags != null && flags.get("png") != null)
            return flags.get("png").toString();
        return null;
    }

    public Map<String, Object> getCurrencies() { return currencies; }

    // === Setters ===
    public void setNameObj(Map<String, Object> nameObj) { this.nameObj = nameObj; }
    public void setCapitalList(List<String> capitalList) { this.capitalList = capitalList; }
    public void setRegion(String region) { this.region = region; }
    public void setPopulation(Long population) { this.population = population; }
    public void setFlags(Map<String, Object> flags) { this.flags = flags; }
    public void setCurrencies(Map<String, Object> currencies) { this.currencies = currencies; }
}
