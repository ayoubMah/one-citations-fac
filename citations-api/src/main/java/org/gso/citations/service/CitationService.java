package org.gso.citations.service;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.gso.citations.model.Citation;
import org.gso.citations.repository.CitationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CitationService {

    private final CitationRepository citationRepository;
    private final MongoTemplate mongoTemplate;

    public Citation createCitation(String text, String author, String submitterId) {
        Citation citation = Citation.builder()
                .text(text)
                .author(author)
                .submitterId(submitterId)
                .submissionDate(LocalDateTime.now())
                .status(Citation.Status.PENDING)
                .build();
        return citationRepository.save(citation);
    }

    public Optional<Citation> getRandomValidatedCitation() {
        return citationRepository.findRandomValidatedCitation();
    }

    public Page<Citation> getPendingCitations(Pageable pageable) {
        Query query = new Query().addCriteria(Criteria.where("status").is(Citation.Status.PENDING)).with(pageable);
        long count = mongoTemplate.count(query, Citation.class);
        return new PageImpl<>(mongoTemplate.find(query, Citation.class), pageable, count);
    }

    public Citation validateCitation(String id, String validatorId) {
        Citation citation = citationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Citation not found"));
        citation.setStatus(Citation.Status.VALIDATED);
        citation.setValidatorId(validatorId);
        citation.setValidationDate(LocalDateTime.now());
        return citationRepository.save(citation);
    }
}
