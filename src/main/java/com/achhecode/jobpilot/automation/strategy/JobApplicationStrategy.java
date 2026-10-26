package com.achhecode.jobpilot.automation.strategy;

import com.achhecode.jobpilot.application.dto.JobApplicationRequest;
import com.achhecode.jobpilot.automation.context.JobAutomationContext;
import com.achhecode.jobpilot.automation.model.AutomationResult;
import com.achhecode.jobpilot.candidate.dto.CandidateProfileResponse;

public interface JobApplicationStrategy {
    String getCompanyId();

    /**
     * Performs the company's workflow using the supplied page.
     * Browser/context creation and lifecycle are owned by achhecode-playwright.
     */
    AutomationResult apply(JobAutomationContext context, JobApplicationRequest request, CandidateProfileResponse candidate);
}
