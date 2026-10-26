package com.achhecode.jobpilot.candidate.controller;

import com.achhecode.jobpilot.candidate.dto.CandidateCreateRequest;
import com.achhecode.jobpilot.candidate.dto.CandidateCreateResponse;
import com.achhecode.jobpilot.candidate.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CandidateCreateResponse createCandidate(
        @Valid @RequestBody CandidateCreateRequest request
    ) {
        return candidateService.createCandidate(request);
    }
}
