package com.achhecode.jobpilot.automation.pages.capgemini;

import java.nio.file.Path;
import java.util.regex.Pattern;
import com.achhecode.jobpilot.candidate.dto.CandidateProfileResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Capgemini application form page. Replace placeholder selectors with verified
 * selectors.
 */
public class CapgeminiApplicationPage {
    private final Page page;

    public CapgeminiApplicationPage(Page page) {
        this.page = page;
    }

    public void fillApplicantDetails(CandidateProfileResponse request) {
        // TODO Verify field names/selectors on the current Capgemini application form.
        fillRequired("input[name='firstName']", firstName(request.fullName()));
        fillRequired("input[name='lastName']", lastName(request.fullName()));
        fillRequired("input[type='email']", request.email());

        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            fillIfPresent("input[type='tel']", request.phoneNumber());
        }
        uploadResumeIfPresent(request.resumePath());
    }

    public boolean isReadyForReview() {
        // Generic HTML validation is a first check, not proof all site-specific fields
        // are complete.
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
            throw new IllegalStateException("Required Capgemini field not found: " + selector);
        }
        locator.fill(value);
    }

    private void fillIfPresent(String selector, String value) {
        Locator locator = page.locator(selector).first();
        if (locator.count() > 0)
            locator.fill(value);
    }

    private String firstName(String fullName) {
        String value = fullName.trim();
        int index = value.indexOf(' ');
        return index < 0 ? value : value.substring(0, index);
    }

    private String lastName(String fullName) {
        String value = fullName.trim();
        int index = value.indexOf(' ');
        return index < 0 ? "" : value.substring(index + 1);
    }
}
