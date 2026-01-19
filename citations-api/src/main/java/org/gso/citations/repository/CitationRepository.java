package org.gso.citations.repository;

import java.util.Optional;

import org.gso.citations.model.Citation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;

public interface CitationRepository extends MongoRepository<Citation, String> {

    // Aggregation to get a random validated citation
    @Aggregation(pipeline = {
            "{ '$match': { 'status': 'VALIDATED' } }",
            "{ '$sample': { 'size': 1 } }"
    })
    Optional<Citation> findRandomValidatedCitation();

}
