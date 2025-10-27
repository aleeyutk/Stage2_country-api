package com.haidara.countryapi.controller;

import com.haidara.countryapi.model.Country;
import com.haidara.countryapi.service.CountryService;
import com.haidara.countryapi.service.ImageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CountryController {
    
    private final CountryService countryService;
    private final ImageService imageService;
    
    public CountryController(CountryService countryService, ImageService imageService) {
        this.countryService = countryService;
        this.imageService = imageService;
    }
    
    @PostMapping("/countries/refresh")
    public ResponseEntity<?> refreshCountries() {
        try {
            countryService.refreshCountries();
            return ResponseEntity.ok(Map.of("message", "Countries refreshed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "error", "External data source unavailable",
                    "details", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }
    
    @GetMapping("/countries")
    public ResponseEntity<?> getCountries(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String sort) {
        
        try {
            List<Country> countries = countryService.getAllCountries(region, currency, sort);
            return ResponseEntity.ok(countries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }
    
    @GetMapping("/countries/{name}")
    public ResponseEntity<?> getCountryByName(@PathVariable String name) {
        try {
            Optional<Country> country = countryService.getCountryByName(name);
            if (country.isPresent()) {
                return ResponseEntity.ok(country.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Country not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }
    
    @DeleteMapping("/countries/{name}")
    public ResponseEntity<?> deleteCountry(@PathVariable String name) {
        try {
            countryService.deleteCountryByName(name);
            return ResponseEntity.ok(Map.of("message", "Country deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Country not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        try {
            Map<String, Object> status = countryService.getStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }
    
    @GetMapping("/countries/image")
    public ResponseEntity<?> getSummaryImage() {
        try {
            File imageFile = imageService.getSummaryImage();
            if (imageFile != null && imageFile.exists()) {
                Resource resource = new FileSystemResource(imageFile);
                
                return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"summary.png\"")
                    .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Summary image not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }
}
