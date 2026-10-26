
package com.achhecode.jobpilot.application.dto;

import java.time.Instant;

import com.achhecode.jobpilot.application.model.ApplicationStatus;

public record JobApplicationDto(
        Long id,
        String candidateUsername,
        String companyId,
        String jobUrl,
        String jobTitle,
        ApplicationStatus status,
        String message,
        Instant createdAt,
        Instant updatedAt) {
}
