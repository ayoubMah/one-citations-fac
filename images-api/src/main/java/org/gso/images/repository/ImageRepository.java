package org.gso.images.repository;

import org.gso.images.model.Image;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ImageRepository extends MongoRepository<Image, String> {

    @Aggregation(pipeline = { "{ '$sample': { 'size': 1 } }" })
    Optional<Image> findRandomImage();
}
