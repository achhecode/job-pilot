package com.achhecode.jobpilot.automation.pages.infosys;

import java.nio.file.Path;
import java.util.regex.Pattern;

import com.achhecode.jobpilot.application.dto.JobApplicationRequest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Infosys application form page. Replace placeholder selectors with verified
 * selectors.
 */
public class InfosysApplicationPage {
    private final Page page;

    public InfosysApplicationPage(Page page) {
        this.page = page;
    }

    public void fillApplicantDetails(JobApplicationRequest request) {
        // TODO Verify field names/selectors on the current Infosys application form.
        fillRequired("input[name='name']", request.fullName());
        fillRequired("input[type='email']", request.email());

        if (request.phone() != null && !request.phone().isBlank()) {
            fillIfPresent("input[type='tel']", request.phone());
        }
        uploadResumeIfPresent(request.resumePath());
    }

    public boolean isReadyForReview() {
        return page.locator("input:invalid, select:invalid, textarea:invalid").count() == 0;
    }

    public void submit() {
        // TODO Replace with the verified final-submit button selector.
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Submit").setExact(false)).click();
    }

    public boolean isSubmissionConfirmed() {
        // TODO Replace with a reliable site-specific confirmation message/reference.
        return page.getByText(
                Pattern.compile("application submitted", Pattern.CASE_INSENSITIVE)).count() > 0;
    }

    private void uploadResumeIfPresent(String resumePath) {
        if (resumePath == null || resumePath.isBlank())
            return;
        Locator input = page.locator("input[type='file']").first();
        if (input.count() == 0) {
            throw new IllegalStateException("Resume upload control was not found.");
        }
        input.setInputFiles(Path.of(resumePath));
    }

    private void fillRequired(String selector, String value) {
        Locator locator = page.locator(selector).first();
        if (locator.count() == 0) {
            throw new IllegalStateException("Required Infosys field not found: " + selector);
        }
        locator.fill(value);
    }

    private void fillIfPresent(String selector, String value) {
        Locator locator = page.locator(selector).first();
        if (locator.count() > 0)
            locator.fill(value);
    }
}
