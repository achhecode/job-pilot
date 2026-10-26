package com.achhecode.jobpilot.application.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.achhecode.jobpilot.company.CompanyCareerSite;

public record JobApplicationRequest(
        String jobId,
        String jobUrl,

        @NotNull CompanyCareerSite company,

        @NotBlank String candidateUsername,

        Boolean submitAutomatically) {

    public JobApplicationRequest {
        if (submitAutomatically == null) {
            submitAutomatically = true;
        }
    }

    @AssertTrue(message = "Either companyId or jobUrl must be provided")
    public boolean isCompanyIdOrJobUrlProvided() {
        return (jobId != null && !jobId.isBlank())
                || (jobUrl != null && !jobUrl.isBlank());
    }
}
