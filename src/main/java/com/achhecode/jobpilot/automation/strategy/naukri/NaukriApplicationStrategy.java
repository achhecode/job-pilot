package com.achhecode.jobpilot.automation.strategy.naukri;

import org.springframework.stereotype.Component;

import com.achhecode.jobpilot.application.dto.JobApplicationRequest;
import com.achhecode.jobpilot.automation.context.JobAutomationContext;
import com.achhecode.jobpilot.automation.model.AutomationResult;
import com.achhecode.jobpilot.automation.pages.naukri.NaukriHomePage;
import com.achhecode.jobpilot.automation.pages.naukri.NaukriInboxPage;
import com.achhecode.jobpilot.automation.pages.naukri.NaukriJobPage;
import com.achhecode.jobpilot.automation.strategy.JobApplicationStrategy;
import com.achhecode.jobpilot.candidate.dto.CandidateProfileResponse;
import com.achhecode.jobpilot.company.CompanyCareerSite;

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
            var naukriHomePage = new NaukriHomePage(page);
            var naukriInboxPage = new NaukriInboxPage(page);
            var applicationPage = new NaukriJobPage(page);

            // context.page().navigate(selectedJobUrl.toString());

            naukriInboxPage.processUnappliedJobs(2);

            if (!request.submitAutomatically()) {
                return AutomationResult.requiresReview(
                        "Form filled. Review the application and confirm before final submission.",
                        selectedJobUrl.toString());
            }

            if (applicationPage.isSubmissionConfirmed()) {
                return AutomationResult.submitted(
                        "Submission confirmation was detected.", selectedJobUrl.toString());
            }

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
