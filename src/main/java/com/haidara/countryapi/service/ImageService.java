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
            long total = countryRepository.count();
            List<Country> top = countryRepository.findTop5ByOrderByEstimatedGdpDesc();
            String last = countryRepository.findLastRefreshTime()
                    .map(t -> t.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .orElse("Never");

            BufferedImage img = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 600, 400);

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Country GDP Summary", 50, 50);

            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Total Countries: " + total, 50, 90);
            g.drawString("Last Refreshed: " + last, 50, 115);

            g.drawString("Top 5 Countries by GDP:", 50, 150);

            g.setFont(new Font("Arial", Font.PLAIN, 14));
            int y = 180;
            for (int i = 0; i < Math.min(top.size(), 5); i++) {
                Country c = top.get(i);
                String line = String.format("%d. %s - $%.2f", i + 1, c.getName(), c.getEstimatedGdp());
                g.drawString(line, 70, y);
                y += 25;
            }
            g.dispose();

            File output = new File("cache/summary.png");
            ImageIO.write(img, "png", output);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate summary image", e);
        }
    }

    public File getSummaryImage() {
        File file = new File("cache/summary.png");
        if (!file.exists() || file.length() == 0) generateSummaryImage();
        return file.exists() ? file : null;
    }
}
