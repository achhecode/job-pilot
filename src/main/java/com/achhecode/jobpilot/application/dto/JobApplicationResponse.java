package com.achhecode.jobpilot.application.dto;

import com.achhecode.jobpilot.application.model.ApplicationStatus;
import java.time.Instant;

public record JobApplicationResponse(
        Long id,
        String companyId,
        String jobUrl,
        String jobTitle,
        ApplicationStatus status,
        String message,
        Instant createdAt,
        Instant updatedAt
) {}
