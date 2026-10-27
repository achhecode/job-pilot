package com.achhecode.jobpilot.automation.strategy.naukri;

import org.springframework.stereotype.Component;

import com.achhecode.jobpilot.application.dto.JobApplicationRequest;
import com.achhecode.jobpilot.automation.context.JobAutomationContext;
import com.achhecode.jobpilot.automation.model.AutomationResult;
import com.achhecode.jobpilot.automation.pages.naukri.JobFilter;
import com.achhecode.jobpilot.automation.pages.naukri.JobProcessingSummary;
import com.achhecode.jobpilot.automation.pages.naukri.NaukriJobProcessor;
import com.achhecode.jobpilot.automation.strategy.JobApplicationStrategy;
import com.achhecode.jobpilot.candidate.dto.CandidateProfileResponse;
import com.achhecode.jobpilot.company.CompanyCareerSite;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NaukriApplicationStrategy implements JobApplicationStrategy {
    @Override
    public String getCompanyId() {
        return CompanyCareerSite.NAUKRI.getId();
    }

    @Override
    public AutomationResult apply(
            JobAutomationContext context,
            JobApplicationRequest request,
            CandidateProfileResponse candidate) {

        StringBuilder selectedJobUrl = new StringBuilder(request.company().getUrl());

        if (!request.jobId().isEmpty()) {
            selectedJobUrl = selectedJobUrl.append("/").append(request.jobId());
        }

        try {
            var page = context.page();

            // TODO -> Move to job listing page

            JobFilter filter = JobFilter.of(
                    5, // minExperience : card's MAX experience must be >= 5
                    10, // minSalaryLpa : card's MAX salary must be > 10 (or "Not disclosed")
                    7, // maxDaysOld
                    request.maxJobsToApply(), // maxJobsToInspect (0 = all)
                    "java"); // any keyword must appear in tags

            JobProcessingSummary summary = new NaukriJobProcessor(page).process(filter);

            log.info(summary.toString());

            return AutomationResult.requiresReview(
                    "Submit was clicked, but successful submission could not be verified.",
                    selectedJobUrl.toString());
        } catch (UnsupportedOperationException ex) {
            return AutomationResult.requiresReview(ex.getMessage(), selectedJobUrl.toString());
        } catch (RuntimeException ex) {
            return AutomationResult.failed(
                    "Infosys workflow failed: " + ex.getMessage(), selectedJobUrl.toString());
        }
    }

}
