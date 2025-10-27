package com.haidara.countryapi.service;

import com.haidara.countryapi.model.Country;
import com.haidara.countryapi.repository.CountryRepository;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ImageService {
    
    private final CountryRepository countryRepository;
    
    public ImageService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
        createCacheDirectory();
    }
    
    private void createCacheDirectory() {
        try {
            Files.createDirectories(Paths.get("cache"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create cache directory", e);
        }
    }
    
    public void generateSummaryImage() {
        try {
            long totalCountries = countryRepository.count();
            List<Country> topCountries = countryRepository.findTop5ByOrderByEstimatedGdpDesc();
            String lastRefresh = countryRepository.findLastRefreshTime()
                .map(time -> time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .orElse("Never");
            
            // Create image
            BufferedImage image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            
            // Set background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 600, 400);
            
            // Set font and colors
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            
            // Draw title
            g.drawString("Country GDP Summary", 50, 50);
            
            // Draw total countries
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Total Countries: " + totalCountries, 50, 90);
            g.drawString("Last Refreshed: " + lastRefresh, 50, 115);
            
            // Draw top 5 countries
            g.drawString("Top 5 Countries by GDP:", 50, 150);
            
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            int yPos = 180;
            for (int i = 0; i < Math.min(topCountries.size(), 5); i++) {
                Country country = topCountries.get(i);
                String line = String.format("%d. %s - $%.2f", 
                    i + 1, country.getName(), country.getEstimatedGdp());
                g.drawString(line, 70, yPos);
                yPos += 25;
            }
            
            g.dispose();
            
            // Save image
            File output = new File("cache/summary.png");
            ImageIO.write(image, "png", output);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate summary image", e);
        }
    }
    
    public File getSummaryImage() {
        File imageFile = new File("cache/summary.png");
        return imageFile.exists() ? imageFile : null;
    }
}
