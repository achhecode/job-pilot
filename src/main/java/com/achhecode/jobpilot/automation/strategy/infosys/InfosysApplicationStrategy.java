package com.achhecode.jobpilot.automation.strategy.infosys;

import org.springframework.stereotype.Component;

import com.achhecode.jobpilot.application.dto.JobApplicationRequest;
import com.achhecode.jobpilot.automation.context.JobAutomationContext;
import com.achhecode.jobpilot.automation.model.AutomationResult;
import com.achhecode.jobpilot.automation.pages.infosys.InfosysApplicationPage;
import com.achhecode.jobpilot.automation.pages.infosys.InfosysJobSearchPage;
import com.achhecode.jobpilot.automation.strategy.JobApplicationStrategy;
import com.achhecode.jobpilot.candidate.dto.CandidateProfileResponse;
import com.achhecode.jobpilot.company.CompanyCareerSite;

@Component
public class InfosysApplicationStrategy implements JobApplicationStrategy {
    @Override
    public String getCompanyId() {
        return CompanyCareerSite.INFOSYS.getId();
    }

    @Override
    public AutomationResult apply(
            JobAutomationContext context,
            JobApplicationRequest request,
            CandidateProfileResponse candidate) {
        String selectedJobUrl = null;

        try {
            var page = context.page();
            var searchPage = new InfosysJobSearchPage(page);
            var applicationPage = new InfosysApplicationPage(page);

            if (request.jobUrl() == null || request.jobUrl().isBlank()) {
                searchPage.open(CompanyCareerSite.INFOSYS.getUrl());
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
                    "Infosys workflow failed: " + ex.getMessage(), selectedJobUrl);
        }
    }
}
