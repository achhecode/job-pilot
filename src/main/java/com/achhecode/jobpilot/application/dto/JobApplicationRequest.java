package com.achhecode.jobpilot.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inputs for one job search/application attempt.
 * In production, consider storing profile/resume data separately from each attempt.
 */
public record JobApplicationRequest(
        @NotBlank String companyId,
        @NotBlank String keywords,
        String location,
        String jobUrl,
        String jobTitle,
        @NotBlank @Size(max = 200) String fullName,
        @NotBlank @Email String email,
        String phone,
        String resumePath,
        boolean submitAutomatically
) {}
