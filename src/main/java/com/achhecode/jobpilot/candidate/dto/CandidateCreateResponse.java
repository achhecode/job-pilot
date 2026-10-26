package com.achhecode.jobpilot.candidate.dto;

public record CandidateCreateResponse(
    Long id,
    String username,
    String fullName,
    String message
) {}
