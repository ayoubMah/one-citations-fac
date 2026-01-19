package org.gso.images.endpoint;

import lombok.RequiredArgsConstructor;
import org.gso.images.service.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping(value = "/random", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getRandomImage(
            @RequestParam(defaultValue = "200") int width,
            @RequestParam(defaultValue = "200") int height) {

        return imageService.getRandomImage(width, height)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
