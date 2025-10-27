package com.haidara.countryapi.repository;

import com.haidara.countryapi.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByNameIgnoreCase(String name);
    List<Country> findByRegionIgnoreCase(String region);
    List<Country> findByCurrencyCodeIgnoreCase(String currencyCode);
    boolean existsByNameIgnoreCase(String name);
    
    @Query("SELECT c FROM Country c ORDER BY c.estimatedGdp DESC LIMIT 5")
    List<Country> findTop5ByOrderByEstimatedGdpDesc();
    
    @Query("SELECT MAX(c.lastRefreshedAt) FROM Country c")
    Optional<java.time.LocalDateTime> findLastRefreshTime();
}
