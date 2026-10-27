package com.achhecode.jobpilot.application.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.achhecode.jobpilot.company.CompanyCareerSite;

public record JobApplicationRequest(
        String jobId,
        String jobUrl,

        @NotNull CompanyCareerSite company,

        @NotBlank String candidateUsername,

        @Positive Integer maxJobsToApply,

        Boolean submitAutomatically) {

    public JobApplicationRequest {
        if (maxJobsToApply == null) {
            maxJobsToApply = 1;
        }

        if (submitAutomatically == null) {
            submitAutomatically = true;
        }
    }

    @AssertTrue(message = "Either jobId or jobUrl must be provided")
    public boolean isJobIdOrJobUrlProvided() {
        return (jobId != null && !jobId.isBlank())
                || (jobUrl != null && !jobUrl.isBlank());
    }
}
