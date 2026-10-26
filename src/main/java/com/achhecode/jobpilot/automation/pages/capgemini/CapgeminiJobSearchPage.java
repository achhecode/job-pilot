package com.achhecode.jobpilot.automation.pages.capgemini;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Capgemini career-site search page.
 *
 * Selectors below are placeholders. Inspect the live site and replace them
 * with stable, verified selectors before using this workflow.
 */
public class CapgeminiJobSearchPage {
    private final Page page;

    public CapgeminiJobSearchPage(Page page) {
        this.page = page;
    }

    public void open(String careerUrl) {
        page.navigate(careerUrl);
    }

    public void searchJobs(String keywords, String location) {
        // TODO Replace placeholder selectors with current Capgemini selectors.
        fillIfPresent("[data-testid='job-keywords']", keywords);

        if (location != null && !location.isBlank()) {
            fillIfPresent("[data-testid='job-location']", location);
        }

        Locator searchButton = page.locator("[data-testid='job-search-submit']");
        if (searchButton.count() > 0) {
            searchButton.first().click();
        } else {
            throw new IllegalStateException("Capgemini search button selector must be configured.");
        }
    }

    /**
     * Opens a supplied job URL when the caller already selected a listing.
     * If no job URL is supplied, implement listing matching using the live site's DOM.
     */
    public String selectJob(String jobUrl, String jobTitle) {
        if (jobUrl != null && !jobUrl.isBlank()) {
            page.navigate(jobUrl);
            return page.url();
        }

        // TODO Implement matching a result by jobTitle and returning its URL.
        throw new UnsupportedOperationException(
                "Capgemini result matching by title has not been implemented.");
    }

    public void clickApply() {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Apply").setExact(false)).click();
    }

    private void fillIfPresent(String selector, String value) {
        if (value == null || value.isBlank()) return;
        Locator locator = page.locator(selector).first();
        if (locator.count() > 0) locator.fill(value);
        else throw new IllegalStateException("Required Capgemini selector not found: " + selector);
    }
}
