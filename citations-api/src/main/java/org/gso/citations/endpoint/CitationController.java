package org.gso.citations.endpoint;

import lombok.RequiredArgsConstructor;
import org.gso.citations.model.Citation;
import org.gso.citations.service.CitationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/citations")
@RequiredArgsConstructor
public class CitationController {

    private final CitationService citationService;

    @GetMapping("/random")
    public ResponseEntity<Citation> getRandomCitation() {
        return citationService.getRandomValidatedCitation()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('writer')")
    public ResponseEntity<Citation> createCitation(@RequestBody Map<String, String> payload,
            JwtAuthenticationToken principal) {
        String text = payload.get("text");
        String author = payload.get("author");
        String submitterId = principal.getName();

        return ResponseEntity.ok(citationService.createCitation(text, author, submitterId));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('moderator')")
    public ResponseEntity<Page<Citation>> getPendingCitations(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(citationService.getPendingCitations(pageable));
    }

    @PutMapping("/{id}/validate")
    @PreAuthorize("hasRole('moderator')")
    public ResponseEntity<Citation> validateCitation(@PathVariable String id, JwtAuthenticationToken principal) {
        String validatorId = principal.getName();
        return ResponseEntity.ok(citationService.validateCitation(id, validatorId));
    }
}
