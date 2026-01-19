package org.gso.images.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.gso.images.model.Image;
import org.gso.images.repository.ImageRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;

    public Optional<byte[]> getRandomImage(int width, int height) {
        return imageRepository.findRandomImage().map(image -> {
            try {
                if (width > 0 && height > 0) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    Thumbnails.of(new ByteArrayInputStream(image.getData()))
                            .size(width, height)
                            .outputFormat("jpg")
                            .toOutputStream(outputStream);
                    return outputStream.toByteArray();
                }
                return image.getData();
            } catch (IOException e) {
                log.error("Error resizing image", e);
                return image.getData(); // Return original on error
            }
        });
    }

    public void saveImage(Image image) {
        imageRepository.save(image);
    }

    public long count() {
        return imageRepository.count();
    }
}
