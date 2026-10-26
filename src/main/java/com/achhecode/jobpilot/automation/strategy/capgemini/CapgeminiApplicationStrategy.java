package com.achhecode.jobpilot.automation.strategy.capgemini;

import com.achhecode.jobpilot.application.dto.JobApplicationRequest;
import com.achhecode.jobpilot.automation.context.JobAutomationContext;
import com.achhecode.jobpilot.automation.model.AutomationResult;
import com.achhecode.jobpilot.automation.pages.capgemini.CapgeminiApplicationPage;
import com.achhecode.jobpilot.automation.pages.capgemini.CapgeminiJobSearchPage;
import com.achhecode.jobpilot.automation.strategy.JobApplicationStrategy;
import com.achhecode.jobpilot.candidate.dto.CandidateProfileResponse;
import com.achhecode.jobpilot.company.CompanyCareerSite;
import org.springframework.stereotype.Component;

@Component
public class CapgeminiApplicationStrategy implements JobApplicationStrategy {
    @Override
    public String getCompanyId() {
        return CompanyCareerSite.CAPGEMINI.getId();
    }

    @Override
    public AutomationResult apply(
            JobAutomationContext context,
            JobApplicationRequest request,
            CandidateProfileResponse candidate
    ) {
        String selectedJobUrl = null;

        try {
            var page = context.page();
            var searchPage = new CapgeminiJobSearchPage(page);
            var applicationPage = new CapgeminiApplicationPage(page);

            if (request.jobUrl() == null || request.jobUrl().isBlank()) {
                searchPage.open(CompanyCareerSite.CAPGEMINI.getUrl());
                searchPage.searchJobs(candidate.keywords(), candidate.countryCode());
            }

            selectedJobUrl = searchPage.selectJob(request.jobUrl());
            searchPage.clickApply();
            applicationPage.fillApplicantDetails(candidate);

            if (!applicationPage.isReadyForReview()) {
                return AutomationResult.requiresReview(
                        "Form validation indicates missing or invalid fields; inspect the form.",
                        selectedJobUrl);
            }

            // Keep final submission opt-in; default false is safer for a starter.
            if (!request.submitAutomatically()) {
                return AutomationResult.requiresReview(
                        "Form filled. Review the application and confirm before final submission.",
                        selectedJobUrl);
            }

            applicationPage.submit();

            if (applicationPage.isSubmissionConfirmed()) {
                return AutomationResult.submitted(
                        "Submission confirmation was detected.", selectedJobUrl);
            }

            return AutomationResult.requiresReview(
                    "Submit was clicked, but successful submission could not be verified.",
                    selectedJobUrl);
        } catch (UnsupportedOperationException ex) {
            return AutomationResult.requiresReview(ex.getMessage(), selectedJobUrl);
        } catch (RuntimeException ex) {
            return AutomationResult.failed(
                    "Capgemini workflow failed: " + ex.getMessage(), selectedJobUrl);
        }
    }
}
