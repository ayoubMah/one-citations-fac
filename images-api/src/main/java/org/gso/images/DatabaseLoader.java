package org.gso.images;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gso.images.model.Image;
import org.gso.images.service.ImageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseLoader implements CommandLineRunner {

    private final ImageService imageService;

    @Override
    public void run(String... args) throws Exception {
        if (imageService.count() == 0) {
            log.info("No images found. Seeding database...");
            seedImage("https://dummyimage.com/600x400/000/fff.jpg&text=Image+1", "image1.jpg");
            seedImage("https://dummyimage.com/600x400/555/fff.jpg&text=Image+2", "image2.jpg");
            seedImage("https://dummyimage.com/600x400/a00/fff.jpg&text=Image+3", "image3.jpg");
            log.info("Database seeded.");
        }
    }

    private void seedImage(String urlString, String filename) {
        try (InputStream in = java.net.URI.create(urlString).toURL().openStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }

            Image image = Image.builder()
                    .filename(filename)
                    .data(out.toByteArray())
                    .contentType("image/jpeg")
                    .build();
            imageService.saveImage(image);
            log.info("Saved {}", filename);
        } catch (Exception e) {
            log.error("Failed to seed image {}", filename, e);
        }
    }
}
